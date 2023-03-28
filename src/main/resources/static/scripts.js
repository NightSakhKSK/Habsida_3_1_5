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

function updateUserTable() {
    const tableBody = document.getElementById('tableBody');
    tableBody.innerHTML = '';
    fetchUsers();
}

window.showDeleteModal = function(id) {
    let deleteModal = new bootstrap.Modal(document.getElementById('deleteModal'));

    fetch(`/api/admin/getUserById/${id}`)
        .then(response => response.json())
        .then(user => {
            document.getElementById('DUserId').value = user.id;
            document.getElementById('DUsername').value = user.username;
            document.getElementById('DFirstName').value = user.firstName;
            document.getElementById('DLastName').value = user.lastName;
            document.getElementById('Dsalary').value = user.salary;
            document.getElementById('Ddepartment').value = user.department;
            document.getElementById('DUserRole').value = user.roles.map(role => role.name).join(', ');
            document.getElementById('DuserPassword').value = user.password;
            deleteModal.show();
        })
        .catch(error => {
            console.error('Error fetching user:', error);
        });

    var isDelete = false;
    document.getElementById('deleteUser').addEventListener('submit', event => {
        event.preventDefault();
        if (!isDelete) {
            isDelete = true;
            fetch(`/api/admin/deleteUser/${id}`, {
                method: 'DELETE',
                headers: {
                    'Content-Type': 'application/json',
                },
            }).then(() => {
                console.log('User deleted successfully');
                deleteModal.hide();
                document.querySelector('.modal-backdrop').remove();
                updateUserTable(); // обновление таблицы
            }).catch(error => {
                console.error('Error deleting user:', error);
            });
        }
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



window.openEditModal = function(id) {
    let editModal = new bootstrap.Modal(document.getElementById('editModal'));
    const selectedRoles = document.querySelectorAll('#rolesSelect2 option');
    let oldPassword = null; // Добавляем переменную для хранения старого пароля

    fetch(`/api/admin/getUserById/${id}`)
        .then(response => response.json())
        .then(user => {
            document.getElementById('EUserId').value = user.id;
            document.getElementById('EUsername').value = user.username;
            document.getElementById('EFirstName').value = user.firstName;
            document.getElementById('ELastName').value = user.lastName;
            document.getElementById('Esalary').value = user.salary;
            document.getElementById('Edepartment').value = user.department;
            document.getElementById('EuserPassword').value = user.password;
            oldPassword = user.password; // Сохраняем старый пароль

            // Показываем модальное окно
            editModal.show();
        })
        .catch(error => {
            console.error('Error fetching user:', error);
        });

    // Обрабатываем отправку формы
    var isEdit = false;
    document.getElementById('editUser').addEventListener('submit', event => {
        event.preventDefault();

        if (!isEdit) {
            isEdit = true;
            const userId = document.getElementById('EUserId').value;
            const username = document.getElementById('EUsername').value;
            const firstName = document.getElementById('EFirstName').value;
            const lastName = document.getElementById('ELastName').value;
            const salary = document.getElementById('Esalary').value;
            const department = document.getElementById('Edepartment').value;
            const password = document.getElementById('EuserPassword').value;
            const roleIds = Array.from(selectedRoles)
                .filter(option => option.selected)
                .map(option => parseInt(option.value));

            const user = {
                id: parseInt(userId),
                username: username,
                firstName: firstName,
                lastName: lastName,
                salary: parseInt(salary),
                department: department,
                password: password
            };
            const userUpdateDTO = {
                user: user,
                oldPassword: oldPassword, // Добавляем старый пароль в userUpdateDTO
                roleIds: roleIds,
            };

            fetch(`/api/admin/editUser/${userId}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(userUpdateDTO)
            })
                .then(response => {
                    if (response.ok) {
                        // Закрываем модальное окно и обновляем таблицу после успешного сохранения
                        editModal.hide();
                        document.querySelector('.modal-backdrop').remove();
                        updateUserTable(); // обновление таблицы
                    } else {
                        // Обрабатываем ошибку
                        console.log('Error: ' + response.status + ' ' + response.statusText);
                    }
                })
                .catch(error => {
                    console.error('Error updating user:', error);
                });
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

window.showAdminPanel = async function (event) {
    if (event) {
        event.preventDefault();
    }
    document.getElementById("adminPanel").style.display = "block";
    document.getElementById("userView").style.display = "none";

    // Update the active state of the buttons
    document.getElementById("adminPanelBtn").classList.add("active");
    document.getElementById("userViewBtn").classList.remove("active");
}

window.showUserView = async function (event) {
    event.preventDefault();
    document.getElementById("adminPanel").style.display = "none";
    document.getElementById("userView").style.display = "block";

    // Fetch the current user information
    const response = await fetch('/api/admin/currentUser');
    const user = await response.json();

    // Populate the table with user information
    const table = document.querySelector("#userView .table");
    const tbody = table.querySelector("tbody") || table.appendChild(document.createElement("tbody"));
    tbody.innerHTML = `
        <tr>
            <td>${user.id}</td>
            <td>${user.username}</td>
            <td>${user.firstName}</td>
            <td>${user.lastName}</td>
            <td>${user.department}</td>
            <td>${user.salary}</td>
            <td>${user.roles.join(', ')}</td>
        </tr>
    `;
    document.getElementById("adminPanelBtn").classList.remove("active");
    document.getElementById("userViewBtn").classList.add("active");
};

showAdminPanel();

//Отображает пользователей в таблице

async function fetchCurrentUser() {
    try {
        const response = await fetch('/api/admin/currentUser');
        const user = await response.json();
        const usernameEl = document.getElementById('username');
        const userRolesEl = document.getElementById('userRoles');

        // Set the username
        usernameEl.textContent = user.username;

        // Set the user roles
        userRolesEl.textContent = user.roles.join(', ');

    } catch (error) {
        console.error('Error fetching current user:', error);
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

document.addEventListener('DOMContentLoaded', async () => {
    await fetchUsers();
    await fetchCurrentUser()
});
