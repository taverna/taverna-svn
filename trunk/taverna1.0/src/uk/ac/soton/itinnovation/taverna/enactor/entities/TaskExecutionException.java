////////////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2002
//
// Copyright in this library belongs to the IT Innovation Centre of
// 2 Venture Road, Chilworth Science Park, Southampton SO16 7NP, UK.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1
// of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation Inc, 59 Temple Place, Suite 330, Boston MA 02111-1307 USA.
//
//      Created By          :   Darren Marvin
//      Created Date        :   2003/04/07
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: mpocock $
//                              $Date: 2004-05-25 19:45:58 $
//                              $Revision: 1.2 $
//
///////////////////////////////////////////////////////////////////////////////////////
package uk.ac.soton.itinnovation.taverna.enactor.entities;

public class TaskExecutionException extends Exception {
  public TaskExecutionException()
  {
  }

  public TaskExecutionException(String message)
  {
    super(message);
  }

  public TaskExecutionException(Throwable cause)
  {
    super(cause);
  }

  public TaskExecutionException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
