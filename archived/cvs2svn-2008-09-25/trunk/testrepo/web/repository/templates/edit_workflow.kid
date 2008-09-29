<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:py="http://purl.org/kid/ns#"
    py:extends="'master.kid'">

<head>
    <meta content="text/html; charset=UTF-8" http-equiv="content-type" py:replace="''"/>
    <title>Edit workflow $name</title>
</head>

<body>
    <h1>Edit workflow $name</h1>
    <form action="${tg.url('/save_workflow')}" method="post" enctype="multipart/form-data">
        <input type="hidden" name="id" py:attrs="value=id" />
        <label for="name">Name:</label>
            <input type="text" size="30" name="name" py:attrs="value=name"/><br />
        <label for="description">Description:</label>
            <textarea name="description" rows="6" cols="65" py:content="description">
                This is the description
            </textarea><br />
        <input type="submit" name="submit" value="Save"/>
        <!-- FIXME: Cancel button -->
    </form>
    


</body>
</html>
