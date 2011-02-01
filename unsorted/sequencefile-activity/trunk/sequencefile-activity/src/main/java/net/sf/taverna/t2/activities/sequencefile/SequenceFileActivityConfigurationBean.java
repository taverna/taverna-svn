/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.activities.sequencefile;

/**
 * Configuration bean for a SequenceFileActivity.
 * 
 * @author David Withers
 */
public class SequenceFileActivityConfigurationBean {

	private FileFormat fileFormat;

	private SequenceType sequenceType;

	public SequenceFileActivityConfigurationBean() {
		fileFormat = FileFormat.fasta;
		sequenceType = SequenceType.dna;
	}

	public SequenceFileActivityConfigurationBean(SequenceFileActivityConfigurationBean configuration) {
		fileFormat = configuration.fileFormat;
		sequenceType = configuration.sequenceType;
	}

	/**
	 * Returns the fileFormat. The default value is FileFormat.fasta.
	 * 
	 * @return the fileFormat
	 */
	public FileFormat getFileFormat() {
		return fileFormat;
	}

	/**
	 * Sets the value of fileFormat.
	 * 
	 * @param fileFormat
	 *            the new value for fileFormat
	 */
	public void setFileFormat(FileFormat fileFormat) {
		this.fileFormat = fileFormat;
	}

	/**
	 * Returns the sequenceType. The default value is SequenceType.dna.
	 * 
	 * @return the sequenceType
	 */
	public SequenceType getSequenceType() {
		return sequenceType;
	}

	/**
	 * Sets the value of sequenceType.
	 * 
	 * @param sequenceType
	 *            the new value for sequenceType
	 */
	public void setSequenceType(SequenceType sequenceType) {
		this.sequenceType = sequenceType;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fileFormat == null) ? 0 : fileFormat.hashCode());
		result = prime * result + ((sequenceType == null) ? 0 : sequenceType.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (getClass() != obj.getClass()) {
			return false;
		}
		SequenceFileActivityConfigurationBean other = (SequenceFileActivityConfigurationBean) obj;
		if (fileFormat == null) {
			if (other.fileFormat != null) {
				return false;
			}
		} else if (!fileFormat.equals(other.fileFormat)) {
			return false;
		}
		if (sequenceType == null) {
			if (other.sequenceType != null) {
				return false;
			}
		} else if (!sequenceType.equals(other.sequenceType)) {
			return false;
		}
		return true;
	}

}
