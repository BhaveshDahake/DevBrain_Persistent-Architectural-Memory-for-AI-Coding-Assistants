package com.example.DevBrain;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class DevBrainApplication {

	public static void main(String[] args) {
		loadDotEnv();
		SpringApplication.run(DevBrainApplication.class, args);
	}

	private static void loadDotEnv() {
		java.io.File file = new java.io.File(".env");
		if (file.exists()) {
			try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(file))) {
				String line;
				while ((line = reader.readLine()) != null) {
					line = line.trim();
					if (line.isEmpty() || line.startsWith("#")) {
						continue;
					}
					int idx = line.indexOf('=');
					if (idx > 0) {
						String key = line.substring(0, idx).trim();
						String value = line.substring(idx + 1).trim();
						if (value.startsWith("\"") && value.endsWith("\"") && value.length() > 1) {
							value = value.substring(1, value.length() - 1);
						} else if (value.startsWith("'") && value.endsWith("'") && value.length() > 1) {
							value = value.substring(1, value.length() - 1);
						}
						System.setProperty(key, value);
					}
				}
			} catch (Exception e) {
				System.err.println("Failed to load .env: " + e.getMessage());
			}
		}
	}
}
