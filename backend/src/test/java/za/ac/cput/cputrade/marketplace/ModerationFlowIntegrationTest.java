package za.ac.cput.cputrade.marketplace;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import za.ac.cput.cputrade.admin.AdminAction;
import za.ac.cput.cputrade.admin.AdminActionRepository;
import za.ac.cput.cputrade.trust.AppealRepository;
import za.ac.cput.cputrade.trust.AppealStatus;
import za.ac.cput.cputrade.trust.ReportRepository;
import za.ac.cput.cputrade.trust.ReportStatus;
import za.ac.cput.cputrade.user.AccountStatus;
import za.ac.cput.cputrade.user.Role;
import za.ac.cput.cputrade.user.User;

import java.util.List;
import java.util.UUID;

import static com.jayway.jsonpath.JsonPath.read;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ModerationFlowIntegrationTest extends MarketplaceFlowTestSupport {

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AppealRepository appealRepository;

    @Autowired
    private AdminActionRepository adminActionRepository;

    @Test
    void studentCanReportListingAndOnlyAdminCanReviewThePersistedReport() {
        TestUser seller = createUser(Role.VENDOR);
        TestUser reporter = createUser(Role.STUDENT);
        TestUser admin = createUser(Role.ADMIN);
        long listingId = createListing(seller, "Test listing for report moderation");
        String reportJson = """
                {
                  "targetType": "PRODUCT",
                  "targetId": %d,
                  "reason": "SCAM",
                  "details": "The listing appears suspicious."
                }
                """.formatted(listingId);

        ResponseEntity<String> anonymousReport = restTemplate.postForEntity(
                "/api/reports", jsonRequest(null, reportJson), String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, anonymousReport.getStatusCode());

        ResponseEntity<String> reportResponse = restTemplate.postForEntity(
                "/api/reports", jsonRequest(reporter, reportJson), String.class);
        assertEquals(HttpStatus.CREATED, reportResponse.getStatusCode());
        assertEquals("PENDING", read(reportResponse.getBody(), "$.status"));
        long reportId = responseId(reportResponse);

        ResponseEntity<String> forbiddenQueue = restTemplate.exchange(
                "/api/admin/reports", HttpMethod.GET, authRequest(reporter), String.class);
        assertEquals(HttpStatus.FORBIDDEN, forbiddenQueue.getStatusCode());

        ResponseEntity<String> queueResponse = restTemplate.exchange(
                "/api/admin/reports?status=PENDING", HttpMethod.GET, authRequest(admin), String.class);
        assertEquals(HttpStatus.OK, queueResponse.getStatusCode());
        List<Number> reportIds = read(queueResponse.getBody(), "$[*].id");
        assertTrue(reportIds.stream().anyMatch(id -> id.longValue() == reportId));

        ResponseEntity<String> reviewed = restTemplate.exchange(
                "/api/admin/reports/{id}/review", HttpMethod.PATCH,
                authRequest(admin), String.class, reportId);
        assertEquals(HttpStatus.OK, reviewed.getStatusCode());
        assertEquals("REVIEWED", read(reviewed.getBody(), "$.status"));

        var savedReport = reportRepository.findById(reportId).orElseThrow();
        assertEquals(ReportStatus.REVIEWED, savedReport.getStatus());
        assertNotNull(savedReport.getReviewedAt());
    }

    @Test
    void adminActionHistoryIsProtectedAndPersistsSnapshotForModerationActions() {
        TestUser admin = createUser(Role.ADMIN);
        TestUser student = createUser(Role.STUDENT);
        TestUser seller = createUser(Role.VENDOR);
        TestUser appealUser = createUser(Role.STUDENT);
        TestUser rejectedAppealUser = createUser(Role.STUDENT);
        TestUser pendingVendor = createPendingVendor();
        long listingId = createListing(seller, "Test listing for admin audit history");

        ResponseEntity<String> anonymousHistory = restTemplate.getForEntity(
                "/api/admin/history", String.class);
        assertEquals(HttpStatus.UNAUTHORIZED, anonymousHistory.getStatusCode());
        ResponseEntity<String> studentHistory = restTemplate.exchange(
                "/api/admin/history", HttpMethod.GET, authRequest(student), String.class);
        assertEquals(HttpStatus.FORBIDDEN, studentHistory.getStatusCode());

        assertEquals(HttpStatus.OK, restTemplate.exchange(
                "/api/admin/vendor-requests/{id}/approve", HttpMethod.POST,
                authRequest(admin), String.class, pendingVendor.id()).getStatusCode());
        assertEquals(HttpStatus.OK, restTemplate.exchange(
                "/api/admin/listings/{id}/deactivate", HttpMethod.PATCH,
                authRequest(admin), String.class, listingId).getStatusCode());
        assertEquals(HttpStatus.OK, restTemplate.exchange(
                "/api/admin/users/{id}/suspend", HttpMethod.POST,
                jsonRequest(admin, """
                        {"reason":"Audit test","suspendedUntil":null}
                        """), String.class, student.id()).getStatusCode());
        assertEquals(HttpStatus.OK, restTemplate.exchange(
                "/api/admin/users/{id}/reactivate", HttpMethod.POST,
                authRequest(admin), String.class, student.id()).getStatusCode());

        String reportBody = """
                {
                  "targetType": "PRODUCT",
                  "targetId": %d,
                  "reason": "SCAM",
                  "details": "Audit test report."
                }
                """.formatted(listingId);
        long reviewedReportId = responseId(restTemplate.postForEntity(
                "/api/reports", jsonRequest(student, reportBody), String.class));
        long dismissedReportId = responseId(restTemplate.postForEntity(
                "/api/reports", jsonRequest(student, reportBody), String.class));
        assertEquals(HttpStatus.OK, restTemplate.exchange(
                "/api/admin/reports/{id}/review", HttpMethod.PATCH,
                authRequest(admin), String.class, reviewedReportId).getStatusCode());
        assertEquals(HttpStatus.OK, restTemplate.exchange(
                "/api/admin/reports/{id}/dismiss", HttpMethod.PATCH,
                authRequest(admin), String.class, dismissedReportId).getStatusCode());

        for (TestUser appealTarget : List.of(appealUser, rejectedAppealUser)) {
            assertEquals(HttpStatus.OK, restTemplate.exchange(
                    "/api/admin/users/{id}/suspend", HttpMethod.POST,
                    jsonRequest(admin, """
                            {"reason":"Appeal test","suspendedUntil":null}
                            """), String.class, appealTarget.id()).getStatusCode());
        }
        long approvedAppealId = submitAppeal(appealUser.username());
        long rejectedAppealId = submitAppeal(rejectedAppealUser.username());
        assertEquals(HttpStatus.OK, restTemplate.exchange(
                "/api/admin/appeals/{id}/approve", HttpMethod.PATCH,
                authRequest(admin), String.class, approvedAppealId).getStatusCode());
        assertEquals(HttpStatus.OK, restTemplate.exchange(
                "/api/admin/appeals/{id}/reject", HttpMethod.PATCH,
                authRequest(admin), String.class, rejectedAppealId).getStatusCode());

        ResponseEntity<String> history = restTemplate.exchange(
                "/api/admin/history", HttpMethod.GET, authRequest(admin), String.class);
        assertEquals(HttpStatus.OK, history.getStatusCode());
        List<String> actions = read(history.getBody(), "$[*].action");
        assertTrue(actions.containsAll(List.of(
                "VENDOR_APPROVED",
                "LISTING_DEACTIVATED",
                "USER_SUSPENDED",
                "USER_REACTIVATED",
                "REPORT_REVIEWED",
                "REPORT_DISMISSED",
                "APPEAL_APPROVED",
                "APPEAL_REJECTED"
        )));
        List<String> actors = read(history.getBody(), "$[*].actorUsername");
        assertTrue(actors.contains(admin.username()));

        List<AdminAction> entries = adminActionRepository.findAll();
        assertTrue(entries.stream().anyMatch(entry ->
                entry.getAction().name().equals("VENDOR_APPROVED")
                        && entry.getActorUsername().equals(admin.username())
                        && entry.getTargetId().equals(pendingVendor.id())
                        && entry.getTargetLabel().equals(pendingVendor.username())));
        assertTrue(entries.stream().anyMatch(entry ->
                entry.getAction().name().equals("LISTING_DEACTIVATED")
                        && entry.getActorUsername().equals(admin.username())
                        && entry.getTargetId().equals(listingId)
                        && entry.getTargetLabel().equals("Test listing for admin audit history")));
        assertEquals(AppealStatus.APPROVED,
                appealRepository.findById(approvedAppealId).orElseThrow().getStatus());
        assertEquals(AppealStatus.REJECTED,
                appealRepository.findById(rejectedAppealId).orElseThrow().getStatus());
    }

    private TestUser createPendingVendor() {
        String username = "flow_" + UUID.randomUUID().toString().replace("-", "");
        User user = userRepository.saveAndFlush(User.builder()
                .username(username)
                .email(username + "@mycput.ac.za")
                .campusHandle(username + "@cputmarket.ac.za")
                .passwordHash("unused-in-bearer-token-test")
                .role(Role.VENDOR)
                .verified(true)
                .vendorApproved(false)
                .accountStatus(AccountStatus.ACTIVE)
                .build());
        return new TestUser(user.getId(), username, jwtUtil.generateToken(username));
    }

    private long submitAppeal(String username) {
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/appeals",
                jsonRequest(null, """
                        {"username":"%s","message":"Please review my suspension."}
                        """.formatted(username)),
                String.class);
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        return responseId(response);
    }
}
