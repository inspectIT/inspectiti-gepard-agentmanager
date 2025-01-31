import { Connection } from "@/types/Connection";
import ConnectionTable from "./table/ConnectionTable";
import { Page, PageContent, PageHeader, PageTitle } from "@/components/ui/page";
import useConnectionTable from "@/hooks/features/connections/useConnectionTable";
import ConnectionFilters from "./filter/ConnectionFilters";
import TablePagination from "@/components/ui/table-pagination";

interface ConnectionsViewProps {
  connections: Connection[];
}

export default function ConnectionsView({
  connections,
}: Readonly<ConnectionsViewProps>) {
  const table = useConnectionTable(connections);
  return (
    <Page>
      <PageHeader>
        <PageTitle>Connections</PageTitle>
        <ConnectionFilters table={table} />
      </PageHeader>
      <PageContent>
        <ConnectionTable table={table} />
        <TablePagination table={table} />{" "}
      </PageContent>
    </Page>
  );
}
