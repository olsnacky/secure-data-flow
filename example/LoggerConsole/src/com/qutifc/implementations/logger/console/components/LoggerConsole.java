package com.qutifc.implementations.logger.console.components;

import com.qutifc.interfaces.components.Logger;
import com.qutifc.securitycontracts.securitylevels.Low;
import com.qutifc.securitycontracts.types.Channel;

public class LoggerConsole implements Logger {
	@Override
	public void log(String message) {
		Channel<Low> outChannel = new Channel<Low>(value -> System.out.println(value));
		outChannel.writeToChannel(message);
	}

	@Override
	public String type() {
		return "console";
	}
}
