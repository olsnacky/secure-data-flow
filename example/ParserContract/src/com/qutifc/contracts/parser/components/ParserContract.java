package com.qutifc.contracts.parser.components;

import com.qutifc.interfaces.components.Parser;
import com.qutifc.interfaces.components.Record;
import com.qutifc.interfaces.components.Token;
import com.qutifc.interfaces.components.TokenCollection;

public class ParserContract implements Parser {
	private TokenCollection t;
	private Record[] r;
	private int rc;

	@Override
	public void setTokens(TokenCollection tokens) {
		this.t = tokens;
	}

	@Override
	public Record[] getRecords() {
		return this.r;
	}

	@Override
	public void parse() {
		Record[] records = { Record.get() };
		Token token = this.t.getNext();
		if (token != null) {
			this.r = records;
			this.rc = 0;
		}
	}

	@Override
	public int getRecordCount() {
		return this.rc;
	}

	@Override
	public String type() {
		return "contract";
	}
}
