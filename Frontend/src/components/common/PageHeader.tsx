interface PageHeaderProps {
  title: string;
  description?: string;
}

export const PageHeader = ({ title, description }: PageHeaderProps) => (
  <div>
    <h2 className="text-2xl font-semibold text-slate-900">{title}</h2>
    {description ? <p className="mt-1 text-sm text-slate-600">{description}</p> : null}
  </div>
);
