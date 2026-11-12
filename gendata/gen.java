import java.io.*;
import java.nio.file.*;

import com.darwinsys.csv.CSVImport;

Consumer<String> process = line -> {
	List<String> l = new CSVImport().parse(line);
	System.out.printf(
		"insert into member(firstname,lastname,city,province,postcode,email,cellphone) values('%s','%s','%s','%s','%s','%s','%s');\n",
		l.get(0),l.get(1),l.get(4),l.get(5),l.get(6),l.get(9),l.get(7));
};

void main(String[] args) throws IOException {
	int n = 10, 
		skip = 1;			// Header line
	if (args.length == 1) {
		n = Integer.parseInt(args[0]);
	}
	Files.lines(Path.of("ca-500.csv"))
		.skip(skip)
		.limit(n)
		.forEach(process);
}
