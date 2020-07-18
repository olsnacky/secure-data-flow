module com.qutifc.interfaces {
	exports com.qutifc.interfaces.components;

	uses com.qutifc.interfaces.components.Record;
	uses com.qutifc.interfaces.components.Scanner;
	uses com.qutifc.interfaces.components.Parser;
	uses com.qutifc.interfaces.components.TokenCollection;
	uses com.qutifc.interfaces.components.Token;

	requires com.qutifc.securitycontracts;
}