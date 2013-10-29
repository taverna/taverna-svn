package net.sf.taverna.t2.component.registry.standard;

import static java.lang.System.identityHashCode;
import static uk.org.taverna.component.api.Privilege.DOWNLOAD;
import static uk.org.taverna.component.api.Privilege.VIEW;
import net.sf.taverna.t2.component.api.SharingPolicy;
import uk.org.taverna.component.api.Permissions;
import uk.org.taverna.component.api.Permissions.Permission;

abstract class Policy implements SharingPolicy {
	public static final SharingPolicy PUBLIC = new Public();
	public static final SharingPolicy PRIVATE = new Private();

	Policy() {
	}

	public abstract Permissions getPermissionsElement();

	public static SharingPolicy getPolicy(Permissions perm) {
		if (perm == null)
			return PRIVATE;
		if (perm.getGroupPolicyId() != null)
			return new Group(perm.getGroupPolicyId());
		for (Permission p : perm.getPermission())
			if (p.getId() != null)
				return new Group(p.getId().toString(), perm);
		return PUBLIC;
	}

	@Override
	public boolean equals(Object o) {
		if (o == null || !(o instanceof Policy))
			return false;
		return equals((Policy) o);
	}

	@Override
	public abstract int hashCode();

	protected abstract boolean equals(Policy p);

	static class Public extends Policy {
		@Override
		public String getName() {
			return "Public";
		}

		@Override
		public Permissions getPermissionsElement() {
			Permission.Privilege privView = new Permission.Privilege();
			privView.setType(VIEW);
			Permission.Privilege privDownload = new Permission.Privilege();
			privDownload.setType(DOWNLOAD);
			Permission perm = new Permission();
			perm.setCategory("public");
			perm.getPrivilege().add(privView);
			perm.getPrivilege().add(privDownload);
			Permissions result = new Permissions();
			result.getPermission().add(perm);
			return result;
		}

		@Override
		protected boolean equals(Policy p) {
			return p instanceof Public;
		}

		@Override
		public int hashCode() {
			return identityHashCode(PUBLIC);
		}
	}

	static class Private extends Policy {
		@Override
		public String getName() {
			return "Private";
		}

		@Override
		public Permissions getPermissionsElement() {
			return null;
		}

		@Override
		protected boolean equals(Policy p) {
			return p instanceof Private;
		}

		@Override
		public int hashCode() {
			return identityHashCode(PRIVATE);
		}
	}

	static class Group extends Policy {
		private String id;
		private Permissions p;

		public Group(String id) {
			this.id = id;
		}

		public Group(String id, Permissions p) {
			this.id = id;
			this.p = p;
		}

		@Override
		public String getName() {
			return "Group(" + id + ")";
		}

		@Override
		public Permissions getPermissionsElement() {
			if (p != null)
				return p;
			Permissions result = new Permissions();
			result.setGroupPolicyId(id);
			return result;
		}

		@Override
		protected boolean equals(Policy p) {
			return (p instanceof Group) && id.equals(((Group) p).id);
		}

		private static final int BASEHASH = Group.class.hashCode();

		@Override
		public int hashCode() {
			return BASEHASH ^ id.hashCode();
		}
	}
}
