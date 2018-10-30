brat_doc_viewer = function() {

	view = {};

	view.init = function(representation, value){
		try{
			if (!representation.documentText && !representation.terms) {
				document.getElementsByTagName('body')[0].innerHTML = "<h1>No data available</h1>";
				return;
			}
			var tags = representation.tags;
			var title = representation.documentTitle;
			var text = representation.documentText;
			var ids = representation.termIds;
			var terms = representation.terms;
			var startIdx = representation.startIndexes;
			var stopIdx = representation.stopIndexes;
		
			var tagsUnique = tags.filter(function(item, i, ar){ return ar.indexOf(item) === i; });
			var collData = {"entity_types" : []};		
			for (var i = 0; i < tagsUnique.length; i++) {
				var obj = {};
				obj.type = tagsUnique[i];
				obj.labels = [tagsUnique[i]];
				obj.bgColor =  getRandomColor(hashCode(obj.type));
				obj.borderColor = 'darken';
				collData.entity_types.push(obj);
			}
			
			var docData = {"text" : text, entities : []};
			for (var i = 0; i < terms.length; i++) {
				var obj = [ids[i], tags[i]];
				var idx = [[startIdx[i], stopIdx[i]]];
				obj.push(idx);
				docData.entities.push(obj);
			}

			if(!title){
				title = 'Untitled';			
			}
		        var body = document.getElementsByTagName('body')[0];
			var html = "<h1>" + title + "</h1>";
			body.innerHTML = html;
				
			var div = document.createElement('div');
			div.id = 'viz';
			body.appendChild(div);
					
		        Util.embed('viz', collData, docData);
		} catch(err) {
			if (err.stack) {
				alert(err.stack);
			} else {
				alert (err);
			}
		}
		if (parent != undefined && parent.KnimePageLoader != undefined) {
			parent.KnimePageLoader.autoResize(window.frameElement.id);
		}
	}

	function getRandomColor(seed) {
		/*var letters = '0123456789ABCDEF';
		var color = '#';
		for (var i = 0; i < 6; i++) {
			color += letters[Math.floor(Math.random() * 16)];
	  	}*/
		color = Math.floor((Math.abs(Math.sin(seed) * 16777215)) % 16777215);
    		color = color.toString(16);
		// add any colors shorter than 6 characters with leading 0s
		while(color.length < 6) {
			color = '0' + color;
		}
		color = '#' + color;
	  	return color;
	}

	/* taken from https://gist.github.com/hyamamoto/fd435505d29ebfa3d9716fd2be8d42f0 */
	function hashCode(s) {
	  var h = 0, l = s.length, i = 0;
	  if ( l > 0 )
	    while (i < l)
	      h = (h << 5) - h + s.charCodeAt(i++) | 0;
	  return h;
	}

	view.validate = function() {
		return true;
	}

	return view;

}();
