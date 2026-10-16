import { defineConfig } from "@playwright/test";
import path from "node:path";
import { fileURLToPath } from "node:url";

const frontendDirectory = path.dirname(fileURLToPath(import.meta.url));
const backendDirectory = path.resolve(frontendDirectory, "../backend");
const javaHome = process.env.E2E_JAVA_HOME || process.env.JAVA_HOME;
const backendEnvironment = {
  ...process.env,
  SPRING_PROFILES_ACTIVE: "local,test,e2e",
  ...(javaHome
    ? { JAVA_HOME: javaHome, PATH: `${path.join(javaHome, "bin")};${process.env.PATH || ""}` }
    : {}),
};

export default defineConfig({
  testDir: "./e2e",
  fullyParallel: false,
  workers: 1,
  reporter: "list",
  use: {
    baseURL: "http://localhost:5173",
    browserName: "chromium",
    channel: "chrome",
    headless: true,
    trace: "retain-on-failure",
  },
  webServer: [
    {
      command: "mvnw.cmd -q -DskipTests -Dspring-boot.run.useTestClasspath=true -Dspring-boot.run.additional-classpath-elements=target/test-classes test-compile spring-boot:run",
      cwd: backendDirectory,
      url: "http://localhost:8080/api/products",
      timeout: 180_000,
      reuseExistingServer: false,
      env: backendEnvironment,
    },
    {
      command: "npm run dev -- --host localhost --port 5173",
      cwd: frontendDirectory,
      url: "http://localhost:5173",
      timeout: 60_000,
      reuseExistingServer: false,
      env: {
        ...process.env,
        VITE_API_BASE_URL: "http://localhost:8080/api",
      },
    },
  ],
});
