package net.sf.taverna.platform.spring.jdbc;

import org.springframework.jdbc.datasource.DriverManagerDataSource;
import static net.sf.taverna.platform.spring.PropertyInterpolator.interpolate;

/**
 * Trivial subclass of the DriverManagerDataSource so we can use interpolated
 * properties in e.g. database URLs from the spring configuration. Interpolation
 * is applied to all set-able string properties.
 * 
 * @author Tom Oinn
 * 
 */
public class InterpolatingDriverManagerDataSource extends
		DriverManagerDataSource {

	@Override
	public void setUrl(String newUrl) {
		super.setUrl(interpolate(newUrl));
	}
	
	@Override
	public void setUsername(String newUsername) {
		super.setUsername(interpolate(newUsername));
	}
	
	@Override
	public void setPassword(String newPassword) {
		super.setPassword(interpolate(newPassword));
	}
	
	@Override
	public void setDriverClassName(String newDriverClassName) {
		super.setDriverClassName(interpolate(newDriverClassName));
	}
	
}
