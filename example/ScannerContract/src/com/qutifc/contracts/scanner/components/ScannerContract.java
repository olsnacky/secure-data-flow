package com.qutifc.contracts.scanner.components;

import com.qutifc.interfaces.components.Scanner;
import com.qutifc.interfaces.components.Token;
import com.qutifc.interfaces.components.TokenCollection;

public class ScannerContract implements Scanner {
	private String t;
	private TokenCollection tc;

	@Override
	public TokenCollection getTokens() {
		return this.tc;
	}

	@Override
	public void scan() {
		this.tc = null;
		if (this.t != null) {
			Token token = null;
			this.tc.add(token);
		}
	}

	@Override
	public void setText(String text) {
		this.t = text;
	}

	@Override
	public String type() {
		return "contract";
	}
}
