package com.qutifc.simple.clients;

import com.qutifc.securitycontracts.securitylevels.Low;
import com.qutifc.securitycontracts.types.Channel;
import com.qutifc.simple.interfaces.Greeter;

public class Main {
	public static void main(String[] args) {
		Main program = new Main();
		program.run();
	}

	public void run() {
		Greeter greeter = Greeter.get();
		greeter.setGreeting("Hello World!");
		String message = greeter.getGreeting();

		Channel<Low> outChannel = new Channel<Low>(value -> System.out.println(value));
		outChannel.writeToChannel(message);
	}
}