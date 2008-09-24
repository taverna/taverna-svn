<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:py="http://purl.org/kid/ns#"
    py:extends="'master.kid'">

<head>
    <meta content="text/html; charset=UTF-8" http-equiv="content-type" py:replace="''"/>
    <title>Workflow $workflow.name</title>
</head>

<body>

    <h1>Workflow $workflow.name</h1>

    <div class="workflow repository info">
        <h2>Repository info</h2>
        <dl>
            <dt>Name</dt>
            <dd>$workflow.name</dd>

            <dt>Description</dt>
            <dd>$workflow.description</dd>

            <dt>Uploaded</dt>
            <dd>$workflow.created</dd>

            <dt>Uploaded by</dt>
            <dd py:content="workflow.created_by and workflow.created_by.display_name">Creator</dd>

            <dt>Updated</dt>
            <dd>$workflow.updated</dd>
        </dl>
    </div>

    <div py:if="scufl">
        <div class="workflow scufl meta">
            <h2>Metainformation extracted from workflow</h2>
            <dl>
                <dt>Title</dt>
                <dd>$scufl.title</dd>

                <dt>Author</dt>
                <dd>$scufl.author</dd>

                <dt>Description</dt>
                <dd>$scufl.description</dd>

                <dt>LSID</dt>
                <dd>$scufl.lsid</dd>

            </dl>
        </div>
        <div class="workflow scufl port input">
            <h2>Workflow input ports (sources)</h2>
            <ul>
                <li py:for="name in scufl.sources">
                $name
                </li>
            </ul>
        </div>
        <div class="workflow scufl port output">
            <h2>Workflow output ports (sinks)</h2>
            <ul>
                <li py:for="name in scufl.sinks">
                $name
                </li>
            </ul>
        </div>
    </div>    

    <div class="workflow actions">
        <a href="${tg.url('/download/%s' % id)}">Download</a>
        <a href="${tg.url('/edit_workflow/%s' % id)}">Edit</a>
        <a href="${tg.url('/del_workflow/%s' % id)}">Delete</a>
        <a href="${tg.url('/run/%s' % id)}">Run</a>
        <a href="${tg.url('/add_test/%s' % id)}">Add test</a>
    </div>
    <div class="workflow tests" py:if="workflow.tests"> 
        <h2>Defined tests</h2>
        <ul>
            <li py:for="test in workflow.tests">
               $test.name 
               <div class="actions">
                    <a href="${tg.url('/run_test/%s' % test.id)}">Run test</a>
                    <a href="${tg.url('/test/%s' % test.id)}">View test</a>
                    <a href="${tg.url('/edit_test/%s' % test.id)}">Edit test</a>
                    <a href="${tg.url('/del_test/%s' % test.id)}">Delete test</a>
               </div>
            </li>
        </ul>   
    </div>

</body>
</html>
