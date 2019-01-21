package com.teamwizardry.wizardrybot.api.paste;

import javax.annotation.Nullable;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

public class TextLinkExtractor {

	private static Set<TextLink> textLinks = new HashSet<>();

	static {
		textLinks.add(new TextPasteBin());
		textLinks.add(new TextPasteee());
		textLinks.add(new TextHasteBin());
	}

	@Nullable
	public static String getText(URL url) {
		for (TextLink link : textLinks) {
			if (link.test(url)) {
				return link.getText(url);
			}
		}

		return null;
	}
}
