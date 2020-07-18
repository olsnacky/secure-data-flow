package com.qutifc.interfaces.components;

import com.qutifc.securitycontracts.annotations.SecurityContract;

@SecurityContract(ContractName="com.qutifc.contracts.writer.components.WriterContract")
public interface Writer {
	void setReader(Reader reader);
	void write();
	String getText();
	String type();
}
