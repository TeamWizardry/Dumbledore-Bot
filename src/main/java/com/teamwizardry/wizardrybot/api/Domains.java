package com.teamwizardry.wizardrybot.api;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class Domains {

	public static Domains INSTANCE = new Domains();
	public ArrayList<String> domains = new ArrayList<>();

	private Domains() {
	}

	public void init(File file) {
		System.out.println("Processing whitelisted domains...");
		long time = System.currentTimeMillis();

		try (Stream<String> stream = Files.lines(file.toPath())) {
			stream.forEach(line -> {
				if (!StringUtils.isEmpty(line) && line.contains(",")) {
					String domain = StringUtils.substringAfter(line, ",");
					if (StringUtils.isEmpty(domain)) return;
					domains.add(domain);
				}
			});
		} catch (IOException e) {
			System.out.println("Something went wrong.... ->" + e.getMessage());
			e.printStackTrace();
		}

		System.out.println(domains.size() + " whitelisted domains registered successfully! That took " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time) + " seconds");
	}

	public ArrayList<String> getDomains() {
		return domains;
	}

	public boolean isLinkWhitelisted(String link) {
		String domain;
		if (link.startsWith("https://")) {
			domain = StringUtils.substringBetween(link, "https://", "/");
		} else if (link.startsWith("http://")) {
			domain = StringUtils.substringBetween(link, "http://", "/");
		} else return false;


		if (!domain.isEmpty()) {
			for (String dDomain : domains) {
				if (domain.toLowerCase().contains(dDomain.toLowerCase())) {
					return true;
				}
			}
		}
		return false;
	}
}
