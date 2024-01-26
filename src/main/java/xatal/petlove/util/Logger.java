package xatal.petlove.util;

import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.stream.Collectors;

public abstract class Logger {
	private static final String[] mailing_list = System.getenv("EXCEPTION_MAILING_LIST").split(",");

	private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(Logger.class);

	private static final XEmail X_EMAIL =
		new XEmail(System.getenv("EXCEPTION_SENDER"), System.getenv("EXCEPTION_SENDER_PASSWORD"));

	private static void log(Exception exception) {
		Logger.LOGGER.error(exception.getMessage());
	}

	public static void sendException(Exception exception) {
		Logger.X_EMAIL.setFrom("edsonmanuelcarballovera@gmail.com");
		String date = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss").format(new Date());
		Logger.X_EMAIL.setSubject("Excepcion: " + date);
		Logger.X_EMAIL.resetRecipients();
		for (String recipient : Logger.mailing_list) {
			Logger.X_EMAIL.addRecipient("", recipient);
		}
		String body = Arrays.stream(exception.getStackTrace())
			.map(element -> element.toString() + "\n")
			.collect(Collectors.joining());
		Logger.X_EMAIL.setMessage(body);
		Logger.X_EMAIL.send();
	}
}
