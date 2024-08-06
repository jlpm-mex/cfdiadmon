	let consume = (url,message,callback,processId) => {
		$.ajax({
			type:"GET",
			url: url,
			data:message,
			success:function(data,status,xhr){
				if(callback != undefined)
					callback(data,processId,xhr.status);
			}	
		});
	}
	
	let consume_post = (url,message,callback,processId) => {
		$.ajax({
			type:"post",
			enctype: 'multipart/form-data',
			url: url,
			processData : false,
  			contentType : false,
			data:message,
			success:function(data,status,xhr){
				if(callback != undefined)
					callback(data,processId,xhr.status);
			}	
		});
	}
	
	let consume_post_json = (url,message,callback) => {
		$.post({
			type:"POST",
			url: url,
			data:message,
			contentType: "application/json; charset=utf-8",
			success:function(data,status,xhr){
				if(callback != undefined)
					callback(data,xhr.status);
			}	
		});
	}
	
	export {consume,consume_post,consume_post_json};

