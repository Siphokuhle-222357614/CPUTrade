package za.ac.cput.cputrade.e2e;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import za.ac.cput.cputrade.product.Campus;
import za.ac.cput.cputrade.product.Category;
import za.ac.cput.cputrade.product.Condition;
import za.ac.cput.cputrade.product.Product;
import za.ac.cput.cputrade.product.ProductRepository;
import za.ac.cput.cputrade.user.AccountStatus;
import za.ac.cput.cputrade.user.Role;
import za.ac.cput.cputrade.user.User;
import za.ac.cput.cputrade.user.UserRepository;

import java.math.BigDecimal;

@Configuration
@Profile("e2e")
public class E2eSeedConfiguration {

    @Bean
    ApplicationRunner seedCheckoutData(
            UserRepository userRepository,
            ProductRepository productRepository,
            PasswordEncoder passwordEncoder
    ) {
        return args -> {
            User seller = createUser(userRepository, passwordEncoder, "checkout_seller", Role.VENDOR);
            createUser(userRepository, passwordEncoder, "checkout_buyer", Role.STUDENT);
            createUser(userRepository, passwordEncoder, "e2e_admin", Role.ADMIN);

            productRepository.saveAndFlush(Product.builder()
                    .seller(seller)
                    .title("E2E Checkout Item")
                    .description("Seeded only for isolated browser tests")
                    .price(new BigDecimal("125.00"))
                    .category(Category.ELECTRONICS)
                    .condition(Condition.GOOD)
                    .campus(Campus.BELLVILLE)
                    .quantity(5)
                    .active(true)
                    .sold(false)
                    .build());
        };
    }

    private User createUser(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            String username,
            Role role
    ) {
        return userRepository.saveAndFlush(User.builder()
                .username(username)
                .email(username + "@example.test")
                .campusHandle(username + "@cputmarket.ac.za")
                .passwordHash(passwordEncoder.encode("TestPassword123!"))
                .role(role)
                .verified(true)
                .vendorApproved(true)
                .accountStatus(AccountStatus.ACTIVE)
                .build());
    }
}
