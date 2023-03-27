async function fetchRoles() {
    try {
        const response = await fetch('/api/admin/allRoles');
        const roles = await response.json();

        const userRolesSelect = document.getElementById("userRolesSelect");

        roles.forEach(role => {
            const option = document.createElement("option");
            option.value = role.id;
            option.text = role.name;
            userRolesSelect.add(option);
        });
    } catch (error) {
        console.error("Error fetching roles:", error);
    }
}

async function fetchUsers() {
    fetch('/api/admin/users')
        .then(response => response.json())
        .then(users => {
            const tableBody = document.getElementById('tableBody');

            users.forEach(user => {
                const row = document.createElement('tr');

                // Добавляем ячейки с данными пользователя
                row.innerHTML = `
          <td>${user.id}</td>
          <td>${user.username}</td>
          <td>${user.firstName}</td>
          <td>${user.lastName}</td>
          <td>${user.salary}</td>
          <td>${user.department}</td>
          <td>${user.roles.map(role => role.name).join(', ')}</td>
          <td>
            <button type="button" class="btn btn-info" data-toggle="modal" data-target="#editModal" onclick="openEditModal(${user.id})">
              Edit
            </button>
          </td>
          <td>
             <button type="button" class="btn btn-danger" data-toggle="modal" data-target="#deleteModal" onclick="showDeleteModal(${user.id})">
             Delete
             </button>
          </td>
        `;

                tableBody.appendChild(row);
            });
        })
        .catch(error => {
            console.error('Error fetching users:', error);
        });
}

window.showDeleteModal = function(userId) {
    fetch(`/api/admin/getUserById/${userId}`)
        .then(response => response.json())
        .then(user => {
            console.log("User fetched:", user);
            document.getElementById('DUserId').value = user.id;
            document.getElementById('DUsername').value = user.username;
            document.getElementById('DFirstName').value = user.firstName;
            document.getElementById('DLastName').value = user.lastName;
            document.getElementById('Dsalary').value = user.salary;
            document.getElementById('Ddepartment').value = user.department;
            document.getElementById('DUserRole').value = user.roles.map(role => role.name).join(', ');
            document.getElementById('DuserPassword').value = user.password;
            $('#deleteModal').modal('show');
        })
        .catch(error => {
            console.error('Error fetching user:', error);
        });
}
async function submitNewUser() {
    const inputIds = [
        "username1",
        "firstName",
        "lastName",
        "salary",
        "department",
        "userPassword",
        "rolesSelect",
    ];

    // Check if all elements exist before accessing their values
    const inputValues = inputIds.map((id) => {
        const input = document.getElementById(id);
        if (!input) {
            console.error(`Element with id "${id}" not found`);
            return null;
        }
        return input.value;
    });

    // Return early if any element is not found
    if (inputValues.includes(null)) {
        return;
    }

    const [
        username,
        firstName,
        lastName,
        salary,
        department,
        password,
        rolesSelect,
    ] = inputValues;

    // Handle the 'rolesSelect' value separately, as it is not a string value
    const selectedRoles = Array.from(
        document.getElementById("rolesSelect").selectedOptions
    ).map((option) => {
        return {
            id: parseInt(option.value),
            name: option.text,
        };
    });

    const userData = {
        username,
        firstName,
        lastName,
        salary: parseInt(salary),
        department,
        password,
        roles: selectedRoles
        };

    try {
        const response = await fetch("/api/admin/addNewUser", {
            method: "POST",
            headers: {
                "Content-Type": "application/json",
            },
            body: JSON.stringify(userData),
        });

        if (response.ok) {
            alert("User created successfully!");
            location.reload(); // Add this line to refresh the page after the user is created
        } else {
            const errorText = await response.text();
            console.error("Error creating user:", errorText);
            alert("Error creating user: " + errorText);
        }
    } catch (error) {
    }
}

// Обработчик событий для кнопки "Add New User"
document.getElementById("addNewUserBtn2").addEventListener("click", submitNewUser);


function addNewUser() {
    var form = document.getElementById("addUserForm");
    var content = document.getElementById("content");
    var userTable = document.getElementById("userTable");

    if (form.style.display === "none") {
        form.style.display = "block";
        userTable.style.display = "none"; // скрываем таблицу пользователей
    } else {
        form.style.display = "none";
    }

    // сброс значений полей формы
    var inputs = form.querySelectorAll("input[type=text], input[type=password], input[type=number]");
    for (var i = 0; i < inputs.length; i++) {
        inputs[i].value = "";
    }
}

function showUserTable() {
    document.getElementById('userTable').style.display = 'table';
    document.getElementById('addUserForm').style.display = 'none';
}

function toggleAddUserForm() {
    const formDisplay = document.getElementById('addUserForm').style.display;
    document.getElementById('userTable').style.display = formDisplay === 'none' ? 'none' : 'table';
    document.getElementById('addUserForm').style.display = formDisplay === 'none' ? 'block' : 'none';
}

window.submitDeleteForm = async function(userId, roleName) {
    const deleteUserId = document.getElementById('DUserId').value;

    try {
        const response = await fetch(`/api/admin/deleteUser/${userId}?${roleName}`, {
            method: 'DELETE',
            headers: {
                'Content-Type': 'application/json',
                'X-CSRF-TOKEN': document.querySelector('meta[name="csrf-token"]').content
            }
        });
        if (response.status === 200) {
            window.location.reload();
        } else {
            alert('Failed to delete user');
        }
    } catch (error) {
        console.error('Error:', error);
        alert('Failed to delete user');
    }
}

document.getElementById("deleteButton").addEventListener("click", submitDeleteForm);

window.openEditModal = function(userId) {

    fetch(`/api/admin/getUserById/${userId}`)
        .then(response => response.json())
        .then(user => {
            document.getElementById('EUserId').value = user.id;
            document.getElementById('EUsername').value = user.username;
            document.getElementById('EFirstName').value = user.firstName;
            document.getElementById('ELastName').value = user.lastName;
            document.getElementById('Esalary').value = user.salary;
            document.getElementById('Edepartment').value = user.department;
            const userRoleSelect = document.getElementById('rolesSelect2');
            const userRoles = user.roles.map(role => role.name);
            Array.from(userRoleSelect.options).forEach((option) => {
                if (userRoles.includes(option.text)) {
                    option.selected = true;
                }
            });
            document.getElementById('EuserPassword').value = user.password;
        })
        .catch(error => {
            console.error('Error fetching user:', error);
        });
}

function submitEditForm() {
    // Получаем данные формы
    const userId = $('#EUserId').val();
    const username = $('#EUsername').val();
    const firstName = $('#EFirstName').val();
    const lastName = $('#ELastName').val();
    const salary = $('#Esalary').val();
    const department = $('#Edepartment').val();
    const password = $('#EuserPassword').val();
    const selectedRoles = $('#EUserRole option:selected');
    if (selectedRoles.length === 0) {
        alert('Пожалуйста, выберите хотя бы одну роль');
        return;
    }
    const roles = Array.from(selectedRoles).map(option => ({
        id: parseInt(option.value)
    }));

    // Создаем объект пользователя
    const user = {
        id: parseInt(userId),
        username: username,
        firstName: firstName,
        lastName: lastName,
        salary: parseInt(salary),
        department: department,
        password: password,
        roles: roles
    };

    // Отправляем данные на сервер
    fetch('/admin/editUser', {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(user)
    })
        .then(response => {
            if (response.ok) {
                // Закрываем модальное окно и обновляем страницу после успешного сохранения
                $('#editModal').modal('hide');
                location.reload();
            } else {
                // Обрабатываем ошибку
                console.log('Error: ' + response.status + ' ' + response.statusText);
            }
        });
}

function setActiveButton(button) {
    // Удаляем класс 'active' со всех кнопок
    const buttons = document.querySelectorAll('.transparent-btn');
    buttons.forEach(btn => btn.classList.remove('active'));

    // Добавляем класс 'active' к выбранной кнопке
    button.classList.add('active');
}

document.getElementById('showUserTableBtn').addEventListener('click', function () {
    showUserTable();
    setActiveButton(this);
});

document.getElementById('addNewUserBtn').addEventListener('click', function () {
    addNewUser();
    setActiveButton(this);
});

function showAdminPanel(event) {
    if (event) {
        event.preventDefault();
    }
    document.getElementById("adminPanel").style.display = "block";
    document.getElementById("userView").style.display = "none";
}

function showUserView(event) {
    event.preventDefault();
    document.getElementById("adminPanel").style.display = "none";
    document.getElementById("userView").style.display = "block";
}

showAdminPanel();

//Отображает пользователей в таблице
function fetchUserData(userData) {
    try {
        document.getElementById('username').textContent = userData.username;

        let roles = userData.roles.map(role => role.name).join(', ');
        document.getElementById('userRoles').textContent = roles;
    } catch (error) {
        console.error('Error displaying user data:', error);
    }
}

async function fetchCurrentUser() {
    try {
        const response = await fetch('/api/admin/currentUser');
        const user = await response.json();
        return user;
    } catch (error) {
        console.error('Error fetching current user:', error);
    }
}

async function displayUserData() {
    const user = await fetchCurrentUser();
    if (user) {
        fetchUserData(user);
    }
}

document.addEventListener('DOMContentLoaded', async () => {
    await fetchUsers();
    await fetchRoles();
    fetchUserData();
    displayUserData();
});