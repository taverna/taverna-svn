<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:py="http://purl.org/kid/ns#"
    py:extends="'master.kid'">

<head>
    <meta content="text/html; charset=UTF-8" http-equiv="content-type" py:replace="''"/>
    <title>Workflows</title>
</head>

<body>

    <h2>Add a new workflow</h2>

    <form action="new_workflow" method="post" enctype="multipart/form-data">
        <label for="name">Name:</label>
            <input type="text" name="name" py:attrs="value=name"/><br />
        <label for="description">Description:</label>
            <input type="text" name="description" py:attrs="value=description"/><br />
        <label for="upload_file">Workflow file:</label> 
            <input type="file" name="upload_file"/><br/>
        <input type="submit" name="submit" value="Save"/>
    </form>
    


</body>
</html>
