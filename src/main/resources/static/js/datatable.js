$(document).ready(function () {
    $('#myTable').DataTable({
        "order": [],
        "columnDefs": [ {
            "targets"  : 'no-sort',
            "orderable": false,
        }],
        "oLanguage": {
            "sLengthMenu": "Отображать _MENU_ записей",
            "sSearch": "Применить фильтр _INPUT_ к таблице",
            "sInfo":  "Показаны записи с _START_ по _END_. Всего _TOTAL_",
            "sZeroRecords": "Такие записи отсутсвуют",
            "sInfoEmpty": "Не найдено ни одной записи.",
            "sInfoFiltered": "Отфильтровано из 178 записей.",
            "oPaginate": {
                "sFirst":      "Первая страница",
                "sLast":       "Последняя страница",
                "sNext":       "Следующая страница",
                "sPrevious":   "Предыдущая страница"
            },
        },
    });
});