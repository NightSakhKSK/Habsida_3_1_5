async function fetchCurrentUser() {
    try {
        const response = await fetch('/api/user/currentUser');
        const user = await response.json();
        const usernameEl = document.getElementById('username');
        const userRolesEl = document.getElementById('userRoles');

        // Set the username
        usernameEl.textContent = user.username;

        // Set the user roles
        userRolesEl.textContent = user.roles.join(', ');

        // Populate user view table
        populateUserView(user);

    } catch (error) {
        console.error('Error fetching current user:', error);
    }
}

fetchCurrentUser();
function populateUserView(user) {
    const table = document.querySelector(".table");
    const tbody = table.querySelector("tbody");

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
}