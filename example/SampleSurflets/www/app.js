let tasks = [];

window.onload = async function () {
	let raw = await fetch ('/tasks');
	let data = await raw.json ();
	data.tasks.forEach (task => tasks.push (task));
	renderTasks ();
}

function renderTask (task) {
	const li = document.createElement ('li');

	const checkbox = document.createElement ('input');
		  checkbox.setAttribute('type', 'checkbox');
		  checkbox.addEventListener ('change', () => {
			toggleTask (task.id);
		  });

	if (task.completed == true) {
		checkbox.checked = true;
	} else { checkbox.checked = false; }

	const p = document.createElement ('p');
		  p.innerHTML = task.task;

	const button = document.createElement ('button');
		  button.innerHTML = `<i class="fas fa-trash"></i>`;
		  button.addEventListener ('click', () => {
			deleteTask (task.id);
		  });

	li.appendChild (checkbox);
	li.appendChild (p);
	li.appendChild (button);

	return li;

}

function renderTasks () {
	const element = document.getElementById ('tasks');
		  element.innerHTML = '';
	tasks.forEach (task => element.appendChild (renderTask (task)));
	if (tasks.length == 0) {
		element.innerHTML = `<b id="empty">Noch keine ToDo's eingestellt.</b>`
	}
}


async function addNew () {
	const task = prompt ("Bitte die Aufgabe eingeben");
	if (
		task !== undefined && 
		task !== null && 
		typeof task == "string" && 
		task.trim () !== ""
	) {
		let raw = await fetch ('/tasks', {
			method: 'POST',
			body: task.trim ()
		});
		let data = await raw.json ();
		tasks.push (data.task);
		renderTasks ();
	}
}

async function deleteTask (id) {
	let raw = await fetch ('/task', {
		method: 'DELETE',
		body: id
	});
	let data = await raw.json ();
	tasks = tasks.filter (task => task.id !== id)
	renderTasks ();
}

async function toggleTask (id) {
	let raw = await fetch ('/task', {
		method: 'POST',
		body: id
	});
	let data = await raw.json ();
	tasks = tasks.map (task => task.id === id ? data.task : task);
	renderTasks ();
}