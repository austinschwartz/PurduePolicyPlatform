//For the current stage, database is defined by developer. Need to extract data from solr database later.

var database = {};
database["Country"] = ["US", "Japan", "Mexico", "Canada", "France"];
database["Food"] = ["mushrooms", "green peppers", "onions", "tomatoes", "olives"];
database["Animal"] = ["cat", "dog", "pig", "chicken"];
database["Sports"] = ["basketball", "football", "tennis", "pingpong", "volleyball"];

var selected = {};
selected["Country"] = [];
selected["Food"] = [];
selected["Animal"] = [];
selected["Sports"] = [];

//Initialize ..
var left = document.getElementById("left");

for(var attributes in database){
	var h = document.createElement("H2");
	var t = document.createTextNode(attributes);
	h.appendChild(t);

	var p = document.createElement("p");
	p.setAttribute("id", attributes);

	var but = document.createElement("BUTTON");
	but.innerHTML = "Expand";
    but.setAttribute("id", attributes + "Button");
	but.setAttribute("onclick", "expandEvent('" + attributes + "')");

	left.appendChild(h);
    left.appendChild(p);
    left.appendChild(document.createElement('br'));
	left.appendChild(but);
    left.appendChild(document.createElement('br'));
}

//Function that implements the specific funtionality of the website


function expandEvent(attributes){
    if(document.getElementById(attributes).hasChildNodes()){
        document.getElementById(attributes).innerHTML = '';
        document.getElementById(attributes + "Button").innerHTML = "Expand";
    }
    else{
        database[attributes].sort();
    	for(var i in database[attributes]){
            var a = document.createElement('a');
            a.innerHTML += "<br>" + database[attributes][i] + "<br>";
            a.href = "javascript:select('" + attributes + "','" + database[attributes][i] + "');";
            a.id = database[attributes][i];
            document.getElementById(attributes).appendChild(a);
        }
        document.getElementById(attributes + "Button").innerHTML = "Collapse";
    }
}

function select(att, object){
    document.getElementById(object).remove();

    var a = document.createElement('a');
    a.innerHTML += "<br>" + object + "<br>";
    a.href = "javascript:unSelect('" + att + "','" + object + "');";
    a.id = object;
    document.getElementById("selection").appendChild(a);

    database[att].splice(database[att].indexOf(object),1);
    selected[att].push(object);
    loadXMLDoc();
}

function unSelect(att, object){
    document.getElementById(object).remove();
    selected[att].splice(selected[att].indexOf(object),1);
    database[att].push(object);
    database[att].sort();

    var a = document.createElement('a');
    a.innerHTML += "<br>" + object + "<br>";
    a.href = "javascript:select('" + att + "','" + object + "');";
    a.id = object;

    var attRow = document.getElementById(att); 
    attRow.insertBefore(a , attRow.childNodes[database[att].indexOf(object)]);
}

//Send http requst to Server..
function loadXMLDoc()
{
    var xmlhttp;
    if (window.XMLHttpRequest){// code for IE7+, Firefox, Chrome, Opera, Safari
        xmlhttp=new XMLHttpRequest();
    }
    else{// code for IE6, IE5
        xmlhttp=new ActiveXObject("Microsoft.XMLHTTP");
    }
    xmlhttp.onreadystatechange=function(){
        if (xmlhttp.readyState==4 && xmlhttp.status==200){
            //Modify the website here.
            //document.getElementById("docs").innerHTML += xmlhttp.responseText;
            updatepage(xmlhttp.responseText);
        }
    }

    var data = "";

    for(var i in selected){
        for(var j in selected[i]){
            data += selected[i][j] + ".";
        }
    }

    xmlhttp.open("POST","demo_get.asp",true);
    xmlhttp.setRequestHeader("Content-type","application/x-www-form-urlencoded");
    xmlhttp.send(data);
}

function updatepage(JsonString){
    var rsp = eval("(" + JsonString + ")");
    document.getElementById("docs").innerHTML += JsonString + "<br><br><br>" + rsp.response.docs[0].id + "<br>";
}