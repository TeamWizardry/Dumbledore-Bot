package com.teamwizardry.wizardrybot.api.paste;

import javax.annotation.Nullable;
import java.net.URL;

public interface TextLink {

	boolean test(URL url);

	@Nullable
	String getText(URL url);
}
