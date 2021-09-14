public class IndexSuflet implements Surflet {

    @Override
    public void handleRequest (HttpRequest request, HttpResponse response) {

        String name = request.getParameter ("name", "John Doe");

        response.setContentType(HttpRequestBody.ContentType.TEXT_HTML);

        response.append("<html>\n");

            response.append("<head>");
            response.append("</head>\n");

            response.append("<body>\n");
            response.append("<h1>Hallo " + name + "</h1>\n");
            response.append("</body>\n");


        response.append("</html>");
        response.send();

    }

}
