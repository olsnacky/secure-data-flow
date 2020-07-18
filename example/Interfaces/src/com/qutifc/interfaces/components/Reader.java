package com.qutifc.interfaces.components;

import com.qutifc.securitycontracts.annotations.SecurityContract;

@SecurityContract(ContractName="com.qutifc.contracts.reader.components.ReaderContract")
public interface Reader {
	void setText(String text);
	void read();
	Record getNext();
	String type();
}
