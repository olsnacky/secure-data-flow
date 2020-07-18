package com.qutifc.contracts.logger.components;

import com.qutifc.interfaces.components.Logger;
import com.qutifc.securitycontracts.securitylevels.Low;
import com.qutifc.securitycontracts.types.Channel;

public class LoggerContract implements Logger {

	@Override
	public void log(String message) {
		Channel<Low> outChannel = new Channel<Low>(value -> System.out.println(value));
		outChannel.writeToChannel(message);
	}

	@Override
	public String type() {
		return "contract";
	}
}
