package net.sf.taverna.t2.workbench.configuration.mimetype;

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.workbench.configuration.AbstractConfigurable;

public class MimeTypeManager extends AbstractConfigurable {
	
	private static MimeTypeManager instance = new MimeTypeManager();
	
	public static MimeTypeManager getInstance() {
		return instance;
	}

	public String getCategory() {
		return "Mime Type";
	}

	public Map<String, Object> getDefaultPropertyMap() {
		HashMap<String, Object> map = new HashMap<String, Object>();
		map.put("text/plain", "Plain Text");
		map.put("text/xml", "XML Text");
		map.put("text/html", "HTML Text");
		map.put("text/rtf", "Rich Text Format");
		map.put("text/x-graphviz", "Graphviz Dot File");
		map.put("image/png","PNG Image");
		map.put("image/jpeg","JPEG Image");
		map.put("image/gif","GIF Image");
		map.put("application/octet-stream","Binary Data");
		map.put("application/zip","Zip File");
		map.put("chemical/x-swissprot","SWISSPROT Flat File");
		map.put("chemical/x-embl-dl-nucleotide", "EMBL Flat File");
		map.put("chemical/x-ppd","PPD File");
		map.put("chemical/seq-aa-genpept","Genpept Protein");
		map.put("chemical/seq-na-genbank", "Genbank Nucleotide");
		map.put("chemical/x-pdb", "PDB 3D Structure File");
		return map;
	}

	public String getName() {
		return "Mime Type Manager";
	}

	public String getUUID() {
		return "3CBC253F-C62C-4158-AEC6-3017B3C50E59";
	}

}
