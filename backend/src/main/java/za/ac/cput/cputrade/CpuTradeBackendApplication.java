package za.ac.cput.cputrade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class CpuTradeBackendApplication {

	public static void main(String[] args) {
		// CPUTrade is a CPUT-only, single-region app — every LocalDateTime.now()
		// call anywhere in the codebase (createdAt, notifications, ratings, ...)
		// should record Africa/Johannesburg (GMT+2, no DST) wall-clock time,
		// not whatever timezone the host machine happens to default to. This
		// matters most once deployed: most hosting platforms default their
		// container/VM clock to UTC, which would silently shift every
		// timestamp two hours from what South African users actually see.
		// Setting this explicitly, in code, means it's correct everywhere
		// this app runs, dev machine or production host, with no reliance on
		// server configuration outside our control.
		TimeZone.setDefault(TimeZone.getTimeZone("Africa/Johannesburg"));
		SpringApplication.run(CpuTradeBackendApplication.class, args);
	}

}
