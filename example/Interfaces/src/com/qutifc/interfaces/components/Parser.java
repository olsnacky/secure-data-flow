package com.qutifc.interfaces.components;

import java.util.Iterator;
import java.util.ServiceLoader;

import com.qutifc.securitycontracts.annotations.SecurityContract;

@SecurityContract(ContractName = "com.qutifc.contracts.parser.components.ParserContract")
public interface Parser {
	void setTokens(TokenCollection tokens);

	Record[] getRecords();

	void parse();

	int getRecordCount();

	String type();

	public static Parser get(String type) {
		Iterable<Parser> parsers = ServiceLoader.load(Parser.class);

		Iterator<Parser> parserIter = parsers.iterator();
		while (parserIter.hasNext()) {
			Parser parser = parserIter.next();
			if (parser.type().equals(type)) {
				return parser;
			}
		}

		return null;
	}
}
