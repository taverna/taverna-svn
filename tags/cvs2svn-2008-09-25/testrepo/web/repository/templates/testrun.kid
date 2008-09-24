<!DOCTYPE html PUBLIC "-//W3C//DTD XHTML 1.0 Transitional//EN" "http://www.w3.org/TR/xhtml1/DTD/xhtml1-transitional.dtd">
<html xmlns="http://www.w3.org/1999/xhtml" xmlns:py="http://purl.org/kid/ns#"
    py:extends="'master.kid'">

<head>
    <meta content="text/html; charset=UTF-8" http-equiv="content-type" py:replace="''"/>
    <title>Test run of workflow $name</title>
    <style type="text/css">
    <!--
        .list {
            border-left: 1px solid black;
            padding-left: 1em;
        }
        .data img {
            border: #777 thin solid;
            display: block;
        }
    -->
    </style>
</head>

<body>

    <h1>Test run of workflow $name</h1>

    <div class="testrun info">
        <dl>
            <dt>Workflow</dt>
            <dd>
                 <a href="${tg.url('/workflow/%s' % workflow_id)}"
                    py:content="name">Workflow name</a>
                (<a href="${tg.url('/test/%s' % test_id)}">defined test</a>)
            </dd>

        
            <dt>Errors</dt>
            <dd>$errors</dd>

            <dt>Return code from Java</dt>
            <dd>$return_code</dd>
            
            <dt>Passed</dt>
            <dd py:content="' '.join(passed)">tests passed</dd>
            
            <dt>Date</dt>
            <dd py:content="run_date.strftime('%Y-%m-%d')">2006-02-15</dd>
        </dl>
    </div>

    <div py:if="stdout" class="workflow stdout">
        <h2>Stdout</h2>
        <pre py:content="stdout">
            Standard output
        </pre>
    </div>
    <div py:if="stderr" class="workflow stderr">
        <h2>Stderr</h2>
        <pre py:content="stderr">
            Standard Error
        </pre>
    </div>

        <div class="data" py:def="traverse_data(data, stype, url)">
            <div py:if="isinstance(data, basestring)">
                  <pre py:if="'text/' in stype"
                      py:content="data">
                      text/plain and 
                      similar
                  </pre>
                  <img py:if="'image/' in stype" src="$url" />
                  <div py:if="'text/' not in stype and 'image/' not in stype" 
                     class="action">(binary data)</div>
                  <a py:if="len(data) > 70" class="action" href="$url">[download]</a>
              </div>    
              <div class="list" py:if="not isinstance(data, basestring)" 
                   py:for="n,item in enumerate(data)" 
                   py:content="traverse_data(item, stype, '%s/%s' % (url, n))">
              </div>
         </div>

    <div class="workflow input inputs">
        <h2>Inputs</h2> 
        <p py:if="not inputs">No input defined.</p>
        <dl py:if="inputs">
            <div py:for="in_name,input in inputs.iteritems()">
                <dt>$in_name</dt>
                <dd py:content="traverse_data(input.data,
                        input.syntactictype, 
                        '/test_run_download/%s/input/%s' % (id, in_name))">
                    data
                </dd>
            </div>
        </dl>
        <!-- pre py:content="inputdoc">
            Input document
        </pre -->
    </div>
    <div class="workflow output outputs">
        <h2>Output</h2> 
        <p py:if="not outputs">No output returned.</p>
        <dl py:if="outputs">
            <div py:for="out_name,output in outputs.iteritems()">
                <dt>$out_name</dt>
                <dd py:content="traverse_data(output.data,
                                output.syntactictype,
                                '/test_run_download/%s/output/%s' % (id, out_name))">
                    data
                </dd>
            </div>
        </dl>
        <!--pre py:content="outputdoc">
            Output document
        </pre -->
    </div>
    <div py:if="report" class="workflow report">
        <h2>Report</h2>
        <pre py:content="report">
            Report (XML)
        </pre>
    </div>

</body>
</html>
