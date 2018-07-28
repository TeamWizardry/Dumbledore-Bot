package com.teamwizardry.wizardrybot.api;


import ai.api.AIConfiguration;
import ai.api.AIDataService;
import ai.api.AIServiceException;
import ai.api.model.AIRequest;
import ai.api.model.AIResponse;
import ai.api.model.Result;
import com.teamwizardry.wizardrybot.Keys;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class AI {

	public static AI INSTANCE = new AI();

	@NotNull
	private AIDataService dataService;

	private AI() {
		AIConfiguration configuration = new AIConfiguration(Keys.DIALOGFLOW_API);
		dataService = new AIDataService(configuration);

	}

	@Nullable
	public Result think(String message) {
		AIRequest request = new AIRequest(message);
		try {
			AIResponse response = dataService.request(request);
			return response.getResult();
		} catch (AIServiceException | NullPointerException ignored) {
		}
		return null;
	}
}
