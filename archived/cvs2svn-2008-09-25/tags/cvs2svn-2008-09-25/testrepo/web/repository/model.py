from datetime import datetime

from sqlobject import *
from turbogears.database import PackageHub
from turbogears import identity

hub = PackageHub("repository")
__connection__ = hub


class Workflow(SQLObject):
    name = StringCol(length=100)
    created = DateTimeCol(default=datetime.now)
    created_by = ForeignKey("User")
    updated = DateTimeCol(default=datetime.now)
    data = StringCol()
    description = StringCol()
    tests = MultipleJoin("WorkflowTest", joinColumn="workflow_id")
    # Requires this version of taverna
    taverna = StringCol(length=20, default=None)


class WorkflowTest(SQLObject):
    workflow = ForeignKey("Workflow")
    name = StringCol(length=100)
    inputs = MultipleJoin("WorkflowTestInput",
                          joinColumn='workflow_test_id')
    outputs = MultipleJoin("WorkflowTestOutput",
                          joinColumn='workflow_test_id')
    runs = MultipleJoin("WorkflowTestRun", joinColumn='workflow_test_id')
    timeout = IntCol(default=15)


class WorkflowTestInput(SQLObject):
    workflow_test = ForeignKey("WorkflowTest")
    port = StringCol(length=100)
    data = StringCol()


class WorkflowTestOutput(SQLObject):
    workflow_test = ForeignKey("WorkflowTest")
    port = StringCol(length=200)
    type = StringCol(length=32)
    data = StringCol()


class WorkflowTestRun(SQLObject):
    workflow_test = ForeignKey("WorkflowTest")
    run_date = DateTimeCol(default=datetime.now)
    taverna = StringCol(length=20)
    java = StringCol(length=20)
    return_code = IntCol()
    stdout = StringCol()
    stderr = StringCol()
    report = StringCol()
    workflow = StringCol()
    inputdoc = StringCol()
    outputdoc = StringCol()


class VisitIdentity(SQLObject):
    visit_key = StringCol( length=40, alternateID=True,
                          alternateMethodName="by_visit_key" )
    user_id = IntCol()


class Group(SQLObject):
    """
    An ultra-simple group definition.
    """
    
    # names like "Group" and "Order" are reserved words in SQL
    # so we set the name to something safe for SQL
    class sqlmeta:
        table="tg_group"
    
    group_name = UnicodeCol( length=16, alternateID=True,
                            alternateMethodName="by_group_name" )
    display_name = UnicodeCol( length=255 )
    created = DateTimeCol( default=datetime.now )

    # collection of all users belonging to this group
    users = RelatedJoin( "User", intermediateTable="user_group",
                        joinColumn="group_id", otherColumn="user_id" )

    # collection of all permissions for this group
    permissions = RelatedJoin( "Permission", joinColumn="group_id", 
                              intermediateTable="group_permission",
                              otherColumn="permission_id" )


class User(SQLObject):
    """
    Reasonably basic User definition. Probably would want additional attributes.
    """
    user_name = UnicodeCol( length=16, alternateID=True,
                           alternateMethodName="by_user_name" )
    email_address = UnicodeCol( length=255, alternateID=True,
                               alternateMethodName="by_email_address" )
    display_name = UnicodeCol( length=255 )
    password = UnicodeCol( length=40 )
    created = DateTimeCol( default=datetime.now )

    # groups this user belongs to
    groups = RelatedJoin( "Group", intermediateTable="user_group",
                         joinColumn="user_id", otherColumn="group_id" )

    def _get_permissions( self ):
        perms = set()
        for g in self.groups:
            perms = perms | set(g.permissions)
        return perms
        
    def _set_password( self, cleartext_password ):
        "Runs cleartext_password through the hash algorithm before saving."
        hash = identity.encrypt_password(cleartext_password)
        self._SO_set_password(hash)
        
    def set_password_raw( self, password ):
        "Saves the password as-is to the database."
        self._SO_set_password(password)



class Permission(SQLObject):
    permission_name = UnicodeCol( length=16, alternateID=True,
                                 alternateMethodName="by_permission_name" )
    description = UnicodeCol( length=255 )
    
    groups = RelatedJoin("Group",
                         intermediateTable="group_permission",
                         joinColumn="permission_id", 
                         otherColumn="group_id" )


