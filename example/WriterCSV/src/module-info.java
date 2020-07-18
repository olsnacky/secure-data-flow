module com.qutifc.implementations.writer.csv {
	requires com.qutifc.interfaces;
	requires com.qutifc.securitycontracts;

	provides com.qutifc.interfaces.components.Writer with com.qutifc.implementations.writer.csv.components.WriterCSV;
}