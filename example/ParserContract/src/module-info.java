module com.qutifc.contracts.parser {
	requires com.qutifc.interfaces;

	provides com.qutifc.interfaces.components.Parser with com.qutifc.contracts.parser.components.ParserContract;
}