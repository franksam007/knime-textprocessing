brat_doc_viewer = function() {

	view = {};
	var _representation = null;
	var VIZ_ID = 'viz';
	var minWidth = 100;
	var minHeight = 100;
	var layoutContainerID = "layoutContainer";
	var containerID = "documentContainer";

	view.init = function(representation, value){
		try{
			_representation = representation;

			if (!_representation.documentText && !_representation.terms) {
				d3.select("body").append("p").text("Error: No data available");
				return;
			}

			d3.select("html").style("width", "100%").style("height", "100%");
			var body = d3.select("body").style("width", "100%").style("height", "100%");

			var tags = _representation.tags;
			var title = _representation.documentTitle;
			var text = _representation.documentText;
			var ids = _representation.termIds;
			var terms = _representation.terms;
			var startIdx = _representation.startIndexes;
			var stopIdx = _representation.stopIndexes;
			var colors = _representation.colors;
			
			var tagsUnique = tags.filter(function(item, i, ar){ return ar.indexOf(item) === i; });
			var collData = {"entity_types" : []};		
			for (var i = 0; i < tagsUnique.length; i++) {
				var obj = {};
				obj.type = tagsUnique[i];
				obj.labels = [tagsUnique[i]];
				obj.bgColor = colors[i];
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

			var prefixTitle = 'Document Title: '
			if(!title){
				title = 'Untitled';			
			}

			body.append("h1").text(prefixTitle + title);

		    Util.embed(VIZ_ID, $.extend({}, collData), $.extend({}, docData));

		    if (parent != undefined && parent.KnimePageLoader != undefined) {
            	parent.KnimePageLoader.autoResize(window.frameElement.id);
        	}
		} catch(err) {
			if (err.stack) {
				alert(err.stack);
			} else {
				alert (err);
			}
		}
	}

	view.validate = function() {
		return true;
	}

	view.getComponentValue = function() {
		return null;
	}

	view.getSVG = function() {
		//var svgElement = document.getElementById("svgID");
		//var svgElement = d3.select("svg")[0][0];
		//knimeService.inlineSvgStyles(svgElement);
		//return (new XMLSerializer()).serializeToString(svgElement);
	}

	return view;

}();
