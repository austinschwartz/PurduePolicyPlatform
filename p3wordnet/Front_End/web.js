//Find the selected the element and retern them as a stream 

var database = {};
database["Country"] = ["US", "Japan", "Mexico", "Canada", "French"];
database["Food"] = ["mushrooms", "green peppers", "onions", "tomatoes", "olives"];
database["Animal"] = ["cat", "dog", "pig", "chicken"];
database["Sports"] = ["basketball", "football", "tennis", "pingpong", "volleyball"];

var arr_sellected = {};

//Server that handle http request and response.
var http = require('http'); 

var fs = require('fs');

var url = require('url');

var path = require('path');

//var solr = require('solr-client');

//var client = solr.createClient();

var getContentType=function(filePath){
	var contentType="";
	var extension=path.extname(filePath);
	switch(extension){
		case ".html":
			contentType= "text/html";
			break;
		case ".js":
			contentType="text/javascript";
			break;
		case ".css":
			contentType="text/css";
			break;
		case ".gif":
			contentType="image/gif";
			break;
		case ".jpg":
			contentType="image/jpeg";
			break;
		case ".png":
			contentType="image/png";
			break;
		case ".ico":
			contentType="image/icon";
			break;
		default:
			contentType="application/octet-stream";
	}
	return contentType; 
}

var httpserver = http.createServer(function (request, response) {
	if (request.method == "GET"){
		var pathName = url.parse(request.url).pathname;
		pathName = "." + pathName;
		if(pathName.charAt(pathName.length-1)=="/"){
			pathName+="index.html";
		}

		console.log(pathName);

		var filePath = pathName;
		fs.exists(filePath, function(exists){
			if(exists){
				fs.readFile(filePath, 'utf-8',function (err, data) {
					if (err) throw err;
					response.writeHead(200, {"Content-Type": getContentType(filePath) });
					response.write(data);
					response.end();
				}); 
			}
			else{
				response.write("<h1>Content-Type: text/html</h1><br>");
				response.end("<h1>404 Not Found</h1>");
			}
		});
	}
	else{
	    request.on('data', function (chunk) {
	        http.get("http://localhost:8983/solr/collection1/select?q=*%3A*&wt=json&indent=true", function(res) {
	          res.on('data', function (solr_data){
	          	console.log("Data: " + solr_data);
	          	response.write(solr_data);
	          	response.end();
	          })
			}).on('error', function(e) {
			  console.log("Got error: " + e.message);
			});
	    });
	    request.on('close', function () {
	    	console.log();
	    });
	}
	
})
httpserver.listen(8080);  
  
console.log('Server running on port 8080.');







 