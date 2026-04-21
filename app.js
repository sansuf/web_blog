
let isLogin = false;

function login() {
    fetch("http://localhost:8080/post/login", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            username: document.getElementById("username").value,
            password: document.getElementById("password").value
        })
    })
        .then(res => res.text())
        .then(data => {
            alert(data);
            if(data === "登录成功") {
                isLogin = true;
            }
        });
}

// 加载文章列表
function loadPosts() {
    fetch("http://localhost:8080/post/list")
    .then(res => res.json())
    .then(data => {
        let list = document.getElementById("list");
        list.innerHTML = "";

        data.forEach(p => {
            let li = document.createElement("li");
            li.innerHTML = `
    ${p.title}：${p.content}
    <button onclick="deletePost(${p.id})">删除</button>
`;
            list.appendChild(li);
        });
    });
}

// 发布文章
function addPost() {

    if(!isLogin) {
        alert("请先登录！");
        return;
    }

    fetch("http://localhost:8080/post/add", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({
            title: document.getElementById("title").value,
            content: document.getElementById("content").value
        })
    })
        .then(() => loadPosts());
}

// 删除文章
function deletePost(id) {
    fetch(`http://localhost:8080/post/delete/${id}`, {
        method: "DELETE"
    })
        .then(() => {
            loadPosts(); // 删除后刷新列表
        });
}

// 页面加载时执行
loadPosts();