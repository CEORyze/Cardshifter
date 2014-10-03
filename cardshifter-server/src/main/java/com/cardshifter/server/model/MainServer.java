package com.cardshifter.server.model;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.cardshifter.ai.AIs;
import com.cardshifter.ai.CardshifterAI;
import com.cardshifter.ai.CompleteIdiot;
import com.cardshifter.ai.ScoringAI;
import com.cardshifter.api.incoming.LoginMessage;
import com.cardshifter.server.clients.ClientIO;
import com.cardshifter.server.main.FakeAIClientTCG;

public class MainServer {
	private static final Logger logger = LogManager.getLogger(MainServer.class);
	
	private final Server server = new Server();
	private final Map<String, CardshifterAI> ais = new HashMap<>();
	
	public Server start() {
		ais.put("loser", new ScoringAI(AIs.loser()));
		ais.put("idiot", new ScoringAI(AIs.idiot()));
		ais.put("old", new CompleteIdiot());
		ais.put("medium", new ScoringAI(AIs.medium()));
		
		try {
			logger.info("Starting Server...");
			
			server.addConnections(new ServerSock(server, 4242));
			server.addConnections(new ServerWeb(server, 4243));
			
			logger.info("Starting Console...");
			CommandHandler commandHandler = new CommandHandler();
			commandHandler.addHandler("exit", command -> System.exit(0));
			ServerConsole console = new ServerConsole(server, commandHandler);
			new Thread(console, "Console-Thread").start();
			console.addHandler("threads", cmd -> showAllStackTraces(server, System.out::println));
			
			ais.entrySet().forEach(entry -> {
				ClientIO tcgAI = new FakeAIClientTCG(server, entry.getValue());
				server.newClient(tcgAI);
				server.getIncomingHandler().perform(new LoginMessage("AI " + entry.getKey()), tcgAI);
			});
			
			logger.info("Started");
		}
		catch (Exception e) {
			logger.error("Initializing Error", e);
		}
		return server;
	}
	
	private void showAllStackTraces(Server server, Consumer<String> output) {
		output.accept("All stack traces:");
		Map<Thread, StackTraceElement[]> allTraces = Thread.getAllStackTraces();
		for (Thread thread : allTraces.keySet()) {
			output.accept(thread.getName());
			this.stackTrace(thread, output);
		}
	}
	
	private void stackTrace(Thread thread, Consumer<String> output) {
		StackTraceElement[] stackTrace = thread.getStackTrace();
		output.accept("Stack trace for thread " + thread.getId() + ": " + thread.getName());
		for (StackTraceElement trace : stackTrace) {
			output.accept(trace.toString());
		}
		output.accept("");
	}
	
}
