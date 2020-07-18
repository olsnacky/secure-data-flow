module com.qutifc.implementations.reader.csv {
	requires com.qutifc.interfaces;
	requires com.qutifc.securitycontracts;

	provides com.qutifc.interfaces.components.Reader with com.qutifc.implementations.reader.csv.components.CSVReader;
}