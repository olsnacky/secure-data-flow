package com.qutifc.clients.components;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.qutifc.interfaces.components.Logger;
import com.qutifc.interfaces.components.Reader;
import com.qutifc.interfaces.components.Writer;
import com.qutifc.securitycontracts.securitylevels.Low;
import com.qutifc.securitycontracts.types.Channel;

public class Main {
	public static void main(String[] args) {
		String[] csv = { "Column1,Column2,Column3\n" + "Row1Value1,Row1Value2,Row1Value3\n"
				+ "Row2Value1,Row2Value2,Row2Value3\n" + "Row3Value1,Row3Value2,Row3Value3" };

		String[] json = { "[" + "{" + "\"Key1\"" + ":" + "\"Item1Value1\"" + "," + "\"Key2\"" + ":" + "\"Item1Value2\""
				+ "," + "\"Key3\"" + ":" + "\"Item1Value3\"" + "}" + "," + "{" + "\"Key1\"" + ":" + "\"Item2Value1\""
				+ "," + "\"Key2\"" + ":" + "\"Item2Value2\"" + "," + "\"Key3\"" + ":" + "\"Item2Value3\"" + "}" + ","
				+ "{" + "\"Key1\"" + ":" + "\"Item3Value1\"" + "," + "\"Key2\"" + ":" + "\"Item3Value2\"" + ","
				+ "\"Key3\"" + ":" + "\"Item3Value3\"" + "}" + "]" };

		Main program = new Main();
		program.run(json);
	}

	public void run(String[] data) {
		Reader reader = getReader("json");
		Writer writer = getWriter("csv");
		Logger logger = getLogger("console");

		Channel<Low> inChannel = new Channel<Low>(data);
		String textToRead = inChannel.readFromChannel();

		reader.setText(textToRead);
		reader.read();

		writer.setReader(reader);
		writer.write();
		logger.log(writer.getText());
	}

	private Reader getReader(String type) {
		Iterable<Reader> readers = ServiceLoader.load(Reader.class);

		Iterator<Reader> readerIter = readers.iterator();
		while (readerIter.hasNext()) {
			Reader reader = readerIter.next();
			if (reader.type().equals(type)) {
				return reader;
			}
		}

		return null;
	}

	private Writer getWriter(String type) {
		Iterable<Writer> writers = ServiceLoader.load(Writer.class);

		Iterator<Writer> writerIter = writers.iterator();
		while (writerIter.hasNext()) {
			Writer writer = writerIter.next();
			if (writer.type().equals(type)) {
				return writer;
			}
		}

		return null;
	}

	private Logger getLogger(String type) {
		Iterable<Logger> loggers = ServiceLoader.load(Logger.class);

		Iterator<Logger> loggerIter = loggers.iterator();
		while (loggerIter.hasNext()) {
			Logger logger = loggerIter.next();
			if (logger.type().equals(type)) {
				return logger;
			}
		}

		return null;
	}
}
