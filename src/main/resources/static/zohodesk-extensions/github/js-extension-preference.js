var repo_vs_subscription_id = {
    
};
var appCredentials = {
    
};
window.onload = function () {
    $("#targetdiv").html("<h3 style=\"color: silver;margin-top: -5px;font-weight: normal;font-size: 30px;float: right;margin-right: 10%;\"> fetching user details...</h3>");
    ZOHODESK.extension.onload().then(function (App) {
    	ZOHODESK.get('database', {'queriableValue':'appInitConfig'}).then(function(response){
            console.log(response);
            var data = response['database.get'].data;
            for (var item in data) {
                var configname = data[item]['key'];
                if(configname==='login'){
                    //document.getElementById(data[item]['value']).checked=true;
                }
                if(configname==='subscriptions'){
                    repo_vs_subscription_id = data[item]['value'];
                    repo_vs_subscription_id = repo_vs_subscription_id === null ? {} : repo_vs_subscription_id;
                }
                if(configname==='credentials'){
                	appCredentials = data[item].value;
                    if(appCredentials!==null && appCredentials.githubUsername){
                        greetUser();
                        getRepos();
                    }
                    else{
                        appCredentials = {};
                        initUser();
                    }
                }
            }
		   })
		   .catch(function(err){
		        console.log(err);
		   });
		});
}
function greetUser(){
    $("#targetdiv").html("<h3 style=\"color: silver;margin-top: -5px;font-weight: normal;font-size: 30px;float: right;margin-right: 10%;\">@"+appCredentials.githubUsername+"</h3>");
}
function initUser(){
     var reqObj = {
               url : `https://api.github.com/user`,
               connectionLinkName : "my_githubcon",
               type : "GET", headers:{},postBody:{}
            };
            ZOHODESK.request(reqObj).then(function(response){
                var responseJSON = JSON.parse(response);
                if(responseJSON["statusCode"]==200){
                    var responseData = JSON.parse(responseJSON["response"]).statusMessage;
                    var loginName = responseData.login;
                    appCredentials.github = {};
                    appCredentials.githubUsername = loginName;
                    greetUser();
                    try {
                        ZOHODESK.set('extension.config', {name : 'credentials', value : JSON.stringify(appCredentials)}).then(function(res){
                            console.log(res);
                            getRepos();
                        }).
                        catch(function(err){
                            console.log(err);
                            getRepos();
                        }).finally(()=>{
                            console.log("for god's sake");
                        });
                    }
                    catch(e){
                        console.log(e);
                        if(appCredentials!==null && appCredentials.githubUsername){
                            getRepos();
                        }
                    }
                }
            }).catch(function(err){
                let id = "err-"+new Date().getTime();
                $('#log-holder').prepend("<div id=\""+id+"\" class=\"error\"><b class=\"errclose\" onclick=\"removeElem(\'#"+id+"\')\">X</b><i>Cannot get user details. Error :<code>"+JSON.stringify(err)+"</code></i><div class=\"errtime\">"+new Date().toLocaleTimeString()+"</div></div>");
                $("#"+id).hide().slideDown();
            });;
}
function getRepos(){
     var reqObj = {
               url : `https://api.github.com/users/${appCredentials.githubUsername}/repos?type=all`,
               connectionLinkName : "my_githubcon",
                type : "GET", headers:{},postBody:{}
            };
            ZOHODESK.request(reqObj).then(function(response){
                var responseJSON = JSON.parse(response);
                if(responseJSON["statusCode"]==200){
                   var data = JSON.parse(JSON.parse(responseJSON["response"]).statusMessage);
                   for (var item in data) {
                        $('.skeleton').hide();
                        $('#table-repodir').append(getRepoRowTemplate(data[item], repo_vs_subscription_id.hasOwnProperty(data[item].full_name)));
                    }
                    initEvents();
                }
            }).catch(function(err){
                let id = "err-"+new Date().getTime();
                $('#log-holder').prepend("<div id=\""+id+"\" class=\"error\"><b class=\"errclose\" onclick=\"removeElem(\'#"+id+"\')\">X</b><i>Cannot get repository details. Error :<code>"+JSON.stringify(err)+"</code></i><div class=\"errtime\">"+new Date().toLocaleTimeString()+"</div></div>");
                $("#"+id).hide().slideDown();
            });
}
function removeElem(id){
    $(id).slideUp();
}
function subscribe(repo_name){
    var subscribe_url = "https://yesiamvj-desk.zcodeusers.com/github/githubCommentListener?appOrgId="+appCredentials.deskOrgId+"&appSecurityContext="+appCredentials.deskSecurityContext;
    var reqObj = {
               url : `https://api.github.com/repos/${repo_name}/hooks`,
               connectionLinkName : "my_githubcon",
               type : "POST", 
               headers:{},
               postBody:{
                            "name": "web",
                            "active": true,
                            "events": [
                                "issues",
                                "issue_comment"
                            ],
                            "config": {
                                "url": subscribe_url,
                                "content_type": "json"
                            }
                        }
            };
            ZOHODESK.request(reqObj).then(function(response){
                var responseJSON = JSON.parse(response);
                if(responseJSON["statusCode"]==200){
                    var data = JSON.parse(responseJSON["response"]).statusMessage;
                    console.log(data);
                    storeToStorage(repo_name, data.id);
                    successSubscribe(repo_name);
                }
            }).catch(function(err){
                let id = "err-"+new Date().getTime();
                $('#log-holder').prepend("<div id=\""+id+"\" class=\"error\"><b class=\"errclose\" onclick=\"removeElem(\'#"+id+"\')\">X</b><i>Error while subscribing to <b>"+repo_name+"</b>. Error :<code>"+err+"</code></i><div class=\"errtime\">"+new Date().toLocaleTimeString()+"</div></div>");
                $("#"+id).hide().slideDown();
            });
}
function un_subscribe(repo_name){
    var hook_id = repo_vs_subscription_id[repo_name];
    var reqObj = {
               url : `https://api.github.com/repos/${repo_name}/hooks/${hook_id}`,
               connectionLinkName : "my_githubcon",
               type : "DELETE", 
               headers:{},
               postBody:{}
            };
            ZOHODESK.request(reqObj).then(function(response){
                var responseJSON = JSON.parse(response);
                if(responseJSON["statusCode"]==200){
                    var data = JSON.parse(responseJSON["response"]).statusMessage;
                    console.log(data);
                    deleteFromStorage(repo_name);
                    successUnSubscribe(repo_name);
                }
            }).catch(function(err){
                let id = "err-"+new Date().getTime();
                $('#log-holder').prepend("<div id=\""+id+"\" class=\"error\"><b class=\"errclose\" onclick=\"removeElem(\'#"+id+"\')\">X</b><i>Error while un-subscribing to <b>"+repo_name+"</b>. Error "+jqXHR.status+" :<code>"+jqXHR.responseText+"</code></i><div class=\"errtime\">"+new Date().toLocaleTimeString()+"</div></div>");
                $("#"+id).hide().slideDown();
            });
}
function storeToStorage(key, value){
    repo_vs_subscription_id[key] = value;
    ZOHODESK.set('database', {'key':'subscriptions','value': repo_vs_subscription_id, 'queriableValue':'appInitConfig'}).then(function(res){
                        console.log(res);
                    }).
                    catch(function(err){
                        console.log(err);
                    });
}
function deleteFromStorage(key){
    delete repo_vs_subscription_id[key];
    ZOHODESK.set('database', {'key':'subscriptions','value': repo_vs_subscription_id, 'queriableValue':'appInitConfig'}).then(function(res){
                        console.log(res);
                    }).
                    catch(function(err){
                        console.log(err);
                    });
}
function successSubscribe(repo_name){
    repo_name = repo_name.replace('.','\\.').replace('#','\\#').replace('/','\\/');
    var elem_id = `#repo-${repo_name} .sub-btn`;
    $(elem_id).removeClass('processing').removeClass('subscribe').addClass('un-subscribe').text('Un-Subscribe').removeAttr('disabled');
}
function successUnSubscribe(repo_name){
    repo_name = repo_name.replace('.','\\.').replace('#','\\#').replace('/','\\/');
    var elem_id = `#repo-${repo_name} .sub-btn`;
    $(elem_id).removeClass('processing').removeClass('un-subscribe').addClass('subscribe').text('Subscribe').removeAttr('disabled');
}
function initEvents(){
    $(document).on('click', '.subscribe', function(e){
        $(e.target).addClass('processing').text('Subscribing..').attr({'disabled':'true'});
        var reponame = $(e.target).attr('data-reponame');
        console.log(reponame);
        e.preventDefault();
        subscribe(reponame);
    });
    $(document).on('click','.un-subscribe', function(e){
        $(e.target).addClass('processing').text('Un-subscribing..').attr({'disabled':'true'});
        var reponame = $(e.target).attr('data-reponame');
         e.preventDefault();
         un_subscribe(reponame);
    });
}
function getRepoRowTemplate(obj, isSubscribed){
    return `<tr id="repo-${obj.full_name}">
                <td title="${obj.language} : ${obj.description}">
                    <a class="link-repo-name" href="${obj.html_url}" target="_blank">${obj.name}</a> <br>
                    <div class="repo-fullname">${obj.full_name}</div>
                </td>
                <td>
                    <button data-reponame="${obj.full_name}" class="sub-btn ${!isSubscribed ? "subscribe" : "un-subscribe"}">
                        ${!isSubscribed ? "Subscribe" : "Un-Subscribe"}
                    </button>
                </td>
            </tr>`;
}