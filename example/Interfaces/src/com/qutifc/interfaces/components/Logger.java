package com.qutifc.interfaces.components;

import com.qutifc.securitycontracts.annotations.SecurityContract;

@SecurityContract(ContractName="com.qutifc.contracts.logger.components.LoggerContract")
public interface Logger {
	void log(String messageLow);
	String type();
}
