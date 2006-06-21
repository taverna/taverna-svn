<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:py="http://purl.org/kid/ns#"
    py:extends="'master.kid'">

<head>
    <meta content="text/html; charset=UTF-8" http-equiv="content-type" py:replace="''"/>
    <title>Test for workflow $name</title>
</head>

<body>
    <h1>Test for workflow $name</h1>
    <form action="${tg.url('/new_test')}" method="post" enctype="multipart/form-data">
        <input type="hidden" name="workflow_id"
               py:attrs="value=workflow_id" />
        <input type="hidden" name="test_id"
               py:attrs="value=id" py:if="id" />
        <label for="name">Name:</label>
            <input type="text" name="name" py:attrs="value=name"/><br />

        <div class="workflow scufl input test">
            <h2>Workflow input values (sources)</h2>
                <div py:for="name in scufl.sources">
                    <label for="input_$name"><strong>$name</strong></label><br />

                    <textarea name="input_$name" rows="6" cols="50"
                              py:content="sources.get(name, '')" />
                </div>
        </div>
        <div class="workflow scufl output test">
            <h2>Expected workflow outputs (sinks)</h2>
                <div py:for="name in scufl.sinks">
                    <label for="output_$name"><strong>$name</strong></label>
                     <label for="outtype_$name">matching </label>
                    <select name="outtype_$name">                   
                      <option py:for="type in match_types"
                      	py:attrs="selected=(type==out_types.get(name, 'exact')) or None">
                         $type
                      </option>
                    </select><br />
                    <textarea name="output_$name" rows="6" cols="50" 
                              py:content="sinks.get(name, '')" />
                </div>
        </div>

        <input type="submit" name="submit" value="Save"/>
        <!-- FIXME: Cancel button -->

    </form>
    


</body>
</html>
