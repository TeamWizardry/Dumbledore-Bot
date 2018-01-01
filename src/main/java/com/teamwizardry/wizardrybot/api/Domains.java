package com.teamwizardry.wizardrybot.api;

import com.teamwizardry.wizardrybot.WizardryBot;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

import static com.teamwizardry.wizardrybot.WizardryBot.DEV_MODE;

public class Domains {

	public static Domains INSTANCE = new Domains();
	public ArrayList<String> domains = new ArrayList<>();

	public URL url;

	private Domains() {
		url = WizardryBot.domains;

		if (!DEV_MODE) {
			try {
				System.out.println("Downloading domain list...");
				ReadableByteChannel rbc = Channels.newChannel(url.openStream());
				FileOutputStream fos = new FileOutputStream("domains.txt");
				fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
				System.out.println("Success!");
			} catch (IOException e) {
				e.printStackTrace();
			}

			File file = new File("domains.txt");
			if (file.exists())
				init();
			else {
				System.out.println("Failed to download domain list.");
			}
		}
	}

	private void init() {
		System.out.println("Processing whitelisted domains...");
		System.out.print("> ");
		long time = System.currentTimeMillis();
		try {
			BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
			String line;
			while ((line = br.readLine()) != null) {
				if (!StringUtils.isEmpty(line) && line.contains(",")) {
					String domain = StringUtils.substringAfter(line, ",");
					if (StringUtils.isEmpty(domain)) continue;
					domains.add(domain);
				}
			}
			br.close();
		} catch (IOException e) {
			System.out.println("Something went wrong.... ->" + e.getMessage());
			e.printStackTrace();
		}
		System.out.println("> Done.");

		System.out.println(domains.size() + " whitelisted domains registered successfully! That took " + TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - time) + " seconds");
	}

	public ArrayList<String> getDomains() {
		return domains;
	}

	public boolean isLinkWhitelisted(String link) {
		if (DEV_MODE) return true;

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
