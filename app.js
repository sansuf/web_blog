
let isLogin = localStorage.getItem("isLogin") === "true";




function login() {
    fetch("http://localhost:8080/post/login", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({
            username: document.getElementById("username").value,
            password: document.getElementById("password").value
        })
    })
        .then(res => res.text())
        .then(data => {

            console.log("返回:", data);

            // 👇 只要不是“登录失败”，就当 token
            if(data !== "登录失败") {
                localStorage.setItem("token", data);
                alert("登录成功");
            } else {
                alert("登录失败");
            }
        });
}

function logout() {
    localStorage.removeItem("token");
    alert("已退出登录");
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

    let token = localStorage.getItem("token");

    if(!token) {
        alert("请先登录！");
        return;
    }

    fetch("http://localhost:8080/post/add", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
            "token": token   // 👈 带上 token
        },
        body: JSON.stringify({
            title: document.getElementById("title").value,
            content: document.getElementById("content").value
        })
    })
    .then(res => res.text())
    .then(data => {
        if (data === "添加成功") {
            loadPosts();
        } else {
            alert(data);
        }
    });
}

// 删除文章
function deletePost(id) {
    let token = localStorage.getItem("token");

    if(!token) {
        alert("请先登录！");
        return;
    }

    fetch(`http://localhost:8080/post/delete/${id}`, {
        method: "DELETE",
        headers: {
            "token": token
        }
    })
        .then(res => res.text())
        .then(data => {
            if (data === "删除成功") {
                loadPosts(); // 删除后刷新列表
            } else {
                alert(data);
            }
        });
}

// 页面加载时执行
loadPosts();