package com.qutifc.interfaces.components;

import java.util.ServiceLoader;

import com.qutifc.securitycontracts.annotations.SecurityContract;

@SecurityContract(ContractName = "com.qutifc.contracts.record.components.RecordContract")
public interface Record {
	void setKeys(String[] keys);

	void setValues(String[] values);

	String[] getKeys();

	String[] getValues();

	int getColumnCount();

	void setColumnCount(int columns);

	static Record get() {
		return ServiceLoader.load(Record.class).findFirst().get();
	}
}
