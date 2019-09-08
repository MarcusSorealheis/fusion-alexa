package com.damiendallimore.fusion.alexa;

import com.amazon.ask.Skill;
import com.amazon.ask.Skills;
import com.amazon.ask.servlet.SkillServlet;
import com.damiendallimore.fusion.alexa.config.Configuration;
import com.damiendallimore.fusion.alexa.handlers.CancelandStopIntentHandler;
import com.damiendallimore.fusion.alexa.handlers.FusionExceptionHandler;
import com.damiendallimore.fusion.alexa.handlers.FusionIntentHandler;
import com.damiendallimore.fusion.alexa.handlers.HelpIntentHandler;
import com.damiendallimore.fusion.alexa.handlers.SessionEndedRequestHandler;
import com.damiendallimore.fusion.alexa.handlers.LaunchRequestHandler;

/**
 * Custom Skill Servlet to handle Alexa requests using the ASK v2 API syntax.
 * 
 * @author damien
 *
 */
public class FusionAlexaServlet extends SkillServlet {

	public FusionAlexaServlet(Configuration configuration) {

		super(getSkill(configuration));
	}

	private static Skill getSkill(Configuration configuration) {
		return Skills.standard()
				.addRequestHandlers(
						new CancelandStopIntentHandler(), 
						new FusionIntentHandler(configuration),
						new HelpIntentHandler(), 
						new LaunchRequestHandler(),
						new SessionEndedRequestHandler())
				.addExceptionHandler(new FusionExceptionHandler())
				.withSkillId(configuration.getAlexaWebServiceSettings().getSkillID()).build();
	}

}
